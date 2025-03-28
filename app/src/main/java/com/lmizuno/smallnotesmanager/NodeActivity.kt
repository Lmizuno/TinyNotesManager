package com.lmizuno.smallnotesmanager

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lmizuno.smallnotesmanager.DBManager.CouchbaseManager
import com.lmizuno.smallnotesmanager.databinding.ActivityNodeBinding
import com.lmizuno.smallnotesmanager.models.Node
import com.lmizuno.smallnotesmanager.models.NodeFactory
import com.lmizuno.smallnotesmanager.Adapters.NodeAdapter
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.lmizuno.smallnotesmanager.models.Folder
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNodeBinding
    private lateinit var dbManager: CouchbaseManager
    private var parentId: String? = null
    private lateinit var nodeAdapter: NodeAdapter

    private val editorActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadNodes() // Refresh the list
        }
    }

    companion object {
        private const val EXTRA_PARENT_ID = "parent_id"
        
        fun createIntent(context: Context, parentId: String?): Intent {
            return Intent(context, NodeActivity::class.java).apply {
                putExtra(EXTRA_PARENT_ID, parentId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize CouchbaseManager
        dbManager = CouchbaseManager.getInstance(this)
        
        parentId = intent.getStringExtra(EXTRA_PARENT_ID)
        setupRecyclerView()
        setupSpeedDial()
        loadNodes()
    }

    private fun setupRecyclerView() {
        nodeAdapter = NodeAdapter(emptyList()) { node ->
            when (node) {
                is Folder -> {
                    // Launch new NodeActivity with this folder's ID
                    startActivity(createIntent(this, node.id))
                }
                else -> {
                    // TODO: Handle note click
                }
            }
        }
        binding.recyclerNodes.layoutManager = LinearLayoutManager(this)
        binding.recyclerNodes.adapter = nodeAdapter
    }

    private fun setupSpeedDial() {
        binding.speedDial.apply {
            addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_add_folder, R.drawable.baseline_folder_24)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, theme))
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
                    .setLabel("Add Folder")
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, theme))
                    .create()
            )

            setOnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.fab_add_folder -> {
                        editorActivityResult.launch(
                            EditorFolderActivity.createIntent(this@NodeActivity, parentId = parentId)
                        )
                        close()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun loadNodes() {
        lifecycleScope.launch {
            try {
                val documents = dbManager.queryByParent(parentId)
                val nodes = documents.mapNotNull { doc ->
                    NodeFactory.fromDocument(doc)
                }

                withContext(Dispatchers.Main) {
                    updateUI(nodes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Handle error
            }
        }
    }

    private fun updateUI(nodes: List<Node>) {
        supportActionBar?.title = if (parentId == null) "Root" else "Folder"
        binding.textNodeCount.text = "Found ${nodes.size} items"
        nodeAdapter.updateNodes(nodes)
    }

    override fun onResume() {
        super.onResume()
        loadNodes() // Refresh when returning to this activity
    }
} 
package com.lmizuno.smallnotesmanager

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.lmizuno.smallnotesmanager.databinding.ActivityNodeBinding
import com.lmizuno.smallnotesmanager.Models.Folder
import com.lmizuno.smallnotesmanager.Adapters.NodeAdapter
import com.leinardi.android.speeddial.SpeedDialActionItem
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import com.lmizuno.smallnotesmanager.Models.Node
import com.lmizuno.smallnotesmanager.viewmodels.NodeViewModel

class NodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNodeBinding
    private lateinit var viewModel: NodeViewModel
    private var parentId: String? = null
    private lateinit var nodeAdapter: NodeAdapter

    private val editorActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loadNodes(parentId) // Refresh the list using ViewModel
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

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[NodeViewModel::class.java]
        
        parentId = intent.getStringExtra(EXTRA_PARENT_ID)
        setupRecyclerView()
        setupSpeedDial()
        setupObservers()
        
        // Load nodes
        viewModel.loadNodes(parentId)
    }

    private fun setupRecyclerView() {
        nodeAdapter = NodeAdapter(emptyList()) { node ->
            when (node) {
                is Folder -> {
                    startActivity(createIntent(this, node.id))
                }
                else -> {
                    editorActivityResult.launch(
                        EditorNoteActivity.createIntent(this, noteId = node.id, parentId = parentId))
                }
            }
        }
        binding.recyclerNodes.layoutManager = LinearLayoutManager(this)
        binding.recyclerNodes.adapter = nodeAdapter
    }

    private fun setupObservers() {
        // Observe nodes LiveData
        viewModel.nodes.observe(this) { nodes ->
            updateUI(nodes)
        }
        
        // Observe loading state
//        viewModel.loading.observe(this) { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        }
        
        // Observe errors
        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupSpeedDial() {
        binding.speedDial.apply {
            mainFabClosedIconColor =
                ResourcesCompat.getColor(resources, R.color.white, theme)

            addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_add_folder, R.drawable.baseline_folder_24)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, theme))
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
                    .setLabel("Add Folder")
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, theme))
                    .create()
            )

            addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_add_note, R.drawable.baseline_edit_square_24)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, theme))
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
                    .setLabel("Add Note")
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
                    R.id.fab_add_note -> {
                        editorActivityResult.launch(
                            EditorNoteActivity.createIntent(this@NodeActivity, parentId = parentId)
                        )
                        close()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun updateUI(nodes: List<Node>) {
        supportActionBar?.title = if (parentId == null) "Root" else "Folder"
        binding.textNodeCount.text = "Found ${nodes.size} items"
        nodeAdapter.updateNodes(nodes)
    }
} 
package com.lmizuno.smallnotesmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.lmizuno.smallnotesmanager.Adapters.NodeAdapter
import com.lmizuno.smallnotesmanager.Models.Folder
import com.lmizuno.smallnotesmanager.Models.Node
import com.lmizuno.smallnotesmanager.databinding.ActivityNodeBinding
import com.lmizuno.smallnotesmanager.viewmodels.NodeViewModel

class NodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNodeBinding
    private lateinit var viewModel: NodeViewModel
    private var currentNodeId: String? = null
    private lateinit var nodeAdapter: NodeAdapter

    private val editorActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loadNodes(currentNodeId) // Refresh the list using ViewModel
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

        currentNodeId = intent.getStringExtra(EXTRA_PARENT_ID)
        setupRecyclerView()
        setupSpeedDial()
        setupObservers()

        // Load nodes
        viewModel.loadNodes(currentNodeId)
    }

    private fun setupRecyclerView() {
        nodeAdapter = NodeAdapter(emptyList()) { node ->
            when (node) {
                is Folder -> {
                    startActivity(createIntent(this, node.id))
                }

                else -> {
                    editorActivityResult.launch(
                        EditorNoteActivity.createIntent(this, noteId = node.id, parentId = currentNodeId)
                    )
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
            mainFabClosedIconColor = ResourcesCompat.getColor(resources, R.color.white, theme)

            addActionItem(
                SpeedDialActionItem.Builder(
                    R.id.fab_note_pres_mode,
                    R.drawable.baseline_play_arrow_24
                ).setFabBackgroundColor(
                        ResourcesCompat.getColor(
                            resources, R.color.secondary, theme
                        )
                    ).setFabImageTintColor(
                        ResourcesCompat.getColor(
                            resources, R.color.white, theme
                        )
                    ).setLabel(getString(R.string.note_pres_mode)).setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(
                        ResourcesCompat.getColor(
                            resources, R.color.secondary, theme
                        )
                    ).create()
            )

            addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_add_note, R.drawable.baseline_edit_square_24)
                    .setFabBackgroundColor(
                        ResourcesCompat.getColor(
                            resources, R.color.primary, theme
                        )
                    )
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
                    .setLabel(getString(R.string.add_note)).setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(
                        ResourcesCompat.getColor(
                            resources, R.color.primary, theme
                        )
                    ).create()
            )

            addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_add_folder, R.drawable.baseline_folder_24)
                    .setFabBackgroundColor(
                        ResourcesCompat.getColor(
                            resources, R.color.primary, theme
                        )
                    )
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, theme))
                    .setLabel(getString(R.string.add_folder)).setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(
                        ResourcesCompat.getColor(
                            resources, R.color.primary, theme
                        )
                    ).create()
            )

            if (currentNodeId != null) {
                addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_edit_folder, R.drawable.baseline_edit_24)
                        .setFabBackgroundColor(
                            ResourcesCompat.getColor(
                                resources, R.color.primary, theme
                            )
                        ).setFabImageTintColor(
                            ResourcesCompat.getColor(
                                resources, R.color.white, theme
                            )
                        ).setLabel(getString(R.string.edit_folder)).setLabelColor(Color.WHITE)
                        .setLabelBackgroundColor(
                            ResourcesCompat.getColor(
                                resources, R.color.primary, theme
                            )
                        ).create()
                )

                addActionItem(
                    SpeedDialActionItem.Builder(R.id.fab_delete_folder, R.drawable.baseline_delete_24)
                        .setFabBackgroundColor(
                            ResourcesCompat.getColor(
                                resources, R.color.primary, theme
                            )
                        ).setLabel(R.string.delete_folder).setLabelColor(Color.WHITE)
                        .setLabelBackgroundColor(
                            ResourcesCompat.getColor(
                                resources, R.color.primary, theme
                            )
                        ).create()
                )
            }

            setOnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.fab_note_pres_mode -> {
                        close()
                        true
                    }

                    R.id.fab_add_note -> {
                        editorActivityResult.launch(
                            EditorNoteActivity.createIntent(this@NodeActivity, parentId = currentNodeId)
                        )
                        close()
                        true
                    }

                    R.id.fab_add_folder -> {
                        editorActivityResult.launch(
                            EditorFolderActivity.createIntent(
                                this@NodeActivity, parentId = currentNodeId
                            )
                        )
                        close()
                        true
                    }

                    R.id.fab_edit_folder -> {
                        editorActivityResult.launch(
                            EditorFolderActivity.createIntent(
                                this@NodeActivity, folderId = currentNodeId
                            )
                        )
                        close()
                        true
                    }

                    R.id.deleteCollection -> {
                        // Should have a dialog where you must type the
                        // name of the folder in order to delete it.

                        close()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun updateUI(nodes: List<Node>) {
        // If we're at root level show "Home", otherwise show the folder name
        if (currentNodeId == null) {
            supportActionBar?.title = getString(R.string.title_home)
        } else {
            // Get the folder name using the parent ID
            viewModel.getNode(currentNodeId!!) { node ->
                supportActionBar?.title = node?.name ?: getString(R.string.folder)
            }
        }

        nodeAdapter.updateNodes(nodes)
    }
} 
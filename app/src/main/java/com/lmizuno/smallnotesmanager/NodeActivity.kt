package com.lmizuno.smallnotesmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.lmizuno.smallnotesmanager.adapters.NodeAdapter
import com.lmizuno.smallnotesmanager.adapters.NodeMoveCallback
import com.lmizuno.smallnotesmanager.databinding.ActivityNodeBinding
import com.lmizuno.smallnotesmanager.models.BreadcrumbItem
import com.lmizuno.smallnotesmanager.models.Folder
import com.lmizuno.smallnotesmanager.models.Node
import com.lmizuno.smallnotesmanager.utils.BreadcrumbManager
import com.lmizuno.smallnotesmanager.utils.ThemeManager
import com.lmizuno.smallnotesmanager.viewmodels.NodeViewModel

class NodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNodeBinding
    private lateinit var viewModel: NodeViewModel
    private var currentNodeId: String? = null
    private lateinit var nodeAdapter: NodeAdapter
    private lateinit var breadcrumbManager: BreadcrumbManager

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
        ThemeManager.getInstance(this).applyTheme()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[NodeViewModel::class.java]
        breadcrumbManager = BreadcrumbManager.getInstance(this)

        currentNodeId = intent.getStringExtra(EXTRA_PARENT_ID)

        // Set up the back button in the action bar if not at root level
        supportActionBar?.setDisplayHomeAsUpEnabled(currentNodeId != null)

        setupRecyclerView()
        setupSpeedDial()
        setupObservers()

        // Load nodes
        viewModel.loadNodes(currentNodeId)
    }

    private fun setupRecyclerView() {
        val nodesList = mutableListOf<Node>()
        nodeAdapter = NodeAdapter(nodesList) { node ->
            when (node) {
                is Folder -> {
                    startActivity(createIntent(this, node.id))
                }

                else -> {
                    editorActivityResult.launch(
                        EditorNoteActivity.createIntent(
                            this, noteId = node.id, parentId = currentNodeId
                        )
                    )
                }
            }
        }

        // Set the ViewModel for the adapter
        nodeAdapter.setViewModel(viewModel)

        binding.recyclerNodes.layoutManager = LinearLayoutManager(this)
        binding.recyclerNodes.adapter = nodeAdapter

        // Attach the ItemTouchHelper with our custom callback
        val callback = NodeMoveCallback(nodeAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.recyclerNodes)

        // No need for edit mode toggle - dragging works via long press
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
                    R.id.fab_note_pres_mode, R.drawable.baseline_play_arrow_24
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
                    SpeedDialActionItem.Builder(
                        R.id.fab_delete_folder, R.drawable.baseline_delete_24
                    ).setFabBackgroundColor(
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
                        // Show dialog to choose presentation mode
                        val options = arrayOf(
                            getString(R.string.note_pres_mode),
                            getString(R.string.note_deep_pres_mode)
                        )

                        AlertDialog.Builder(this@NodeActivity)
                            .setTitle(getString(R.string.choose_presentation_mode))
                            .setItems(options) { _, which ->
                                val presOption = when (which) {
                                    0 -> PRESENTATION_OPTION.SHALLOW
                                    1 -> PRESENTATION_OPTION.RECURSIVE
                                    else -> PRESENTATION_OPTION.SHALLOW
                                }

                                startActivity(
                                    NodePresentationActivity.createIntent(
                                        this@NodeActivity, currentNodeId, presOption
                                    )
                                )
                            }.show()

                        close()
                        true
                    }

                    R.id.fab_add_note -> {
                        editorActivityResult.launch(
                            EditorNoteActivity.createIntent(
                                this@NodeActivity, parentId = currentNodeId
                            )
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

                    R.id.fab_delete_folder -> {
                        // Only allow deletion if we're in a folder (not at root)
                        if (currentNodeId != null) {
                            // Get the current folder to know its name
                            viewModel.getNode(currentNodeId!!) { node ->
                                if (node != null) {
                                    // Create a dialog with EditText for confirmation
                                    val builder = AlertDialog.Builder(this@NodeActivity)
                                    val inflater = layoutInflater
                                    val dialogView =
                                        inflater.inflate(R.layout.dialog_delete_confirmation, null)
                                    val editTextConfirm =
                                        dialogView.findViewById<EditText>(R.id.editTextConfirmName)

                                    builder.setView(dialogView)
                                        .setTitle(getString(R.string.confirm_deletion)).setMessage(
                                            getString(
                                                R.string.delete_folder_confirmation, node.name
                                            )
                                        ).setPositiveButton(getString(R.string.delete)) { _, _ ->
                                            // Check if the entered text matches the folder name
                                            val enteredName = editTextConfirm.text.toString()
                                            if (enteredName == node.name) {
                                                // Names match, proceed with deletion
                                                viewModel.deleteNode(
                                                    currentNodeId!!, node.parentId
                                                ) { success ->
                                                    if (success) {
                                                        // Go back to parent folder
                                                        Toast.makeText(
                                                            this@NodeActivity,
                                                            getString(R.string.folder_deleted),
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        // If we have a parent, navigate back to it
                                                        if (node.parentId != null) {
                                                            val intent = createIntent(
                                                                this@NodeActivity, node.parentId
                                                            )
                                                            startActivity(intent)
                                                        } else {
                                                            // Otherwise go to root
                                                            val intent = createIntent(
                                                                this@NodeActivity, null
                                                            )
                                                            startActivity(intent)
                                                        }
                                                        finish()
                                                    } else {
                                                        Toast.makeText(
                                                            this@NodeActivity,
                                                            getString(R.string.delete_failed),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            } else {
                                                // Names don't match, show error
                                                Toast.makeText(
                                                    this@NodeActivity,
                                                    getString(R.string.name_doesnt_match),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }.setNegativeButton(getString(R.string.cancel), null)

                                    val dialog = builder.create()
                                    dialog.show()
                                } else {
                                    Toast.makeText(
                                        this@NodeActivity,
                                        getString(R.string.folder_not_found),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                this@NodeActivity,
                                getString(R.string.cannot_delete_root),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        close()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun updateBreadcrumbUI(path: List<BreadcrumbItem>) {
        val chipGroup = binding.breadcrumbChipGroup
        chipGroup.removeAllViews()

        // Add home chip
        val homeChip = Chip(this).apply {
            text = getString(R.string.title_home)
            isCheckable = false
            isClickable = true

            // Style the home chip
            setChipBackgroundColorResource(R.color.chip_background)
            setTextColor(ResourcesCompat.getColor(resources, R.color.chip_text, theme))
            setRippleColorResource(R.color.chip_ripple)

            // Highlight if we're at root
            if (currentNodeId == null) {
                setChipBackgroundColorResource(R.color.chip_selected_background)
                setTextColor(ResourcesCompat.getColor(resources, R.color.chip_selected_text, theme))
            }

            // Navigate to root when clicked
            setOnClickListener {
                navigateToFolder(null)
            }
        }
        chipGroup.addView(homeChip)

        // For deep hierarchies, use a truncated view
        if (path.size > 4) {
            // Add separator after home
            // TODO: maybe add separator before each folder chip

            // Add ellipsis chip for truncated folders
            val ellipsisChip = Chip(this).apply {
                text = "..."
                isCheckable = false
                isClickable = true
                setChipBackgroundColorResource(R.color.chip_background)
                setTextColor(ResourcesCompat.getColor(resources, R.color.chip_text, theme))
                setRippleColorResource(R.color.chip_ripple)

                // Show a dropdown menu with all folders when clicked
                setOnClickListener { view ->
                    showPathDropdownMenu(view, path.dropLast(2))
                }
            }
            chipGroup.addView(ellipsisChip)

            // Add separator after ellipsis
            // TODO: maybe add separator before each folder chip

            // Show only the last 2 folders in the path
            path.takeLast(2).forEachIndexed { index, item ->
                val isLastItem = index == path.takeLast(2).size - 1

                val chip = Chip(this).apply {
                    text = item.name
                    isCheckable = false
                    isClickable = true

                    // Style the chip
                    setChipBackgroundColorResource(R.color.chip_background)
                    setTextColor(ResourcesCompat.getColor(resources, R.color.chip_text, theme))
                    setRippleColorResource(R.color.chip_ripple)

                    // Highlight the current folder
                    if (isLastItem) {
                        setChipBackgroundColorResource(R.color.chip_selected_background)
                        setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.chip_selected_text,
                                theme
                            )
                        )
                    }

                    // Navigate to this folder when clicked
                    setOnClickListener {
                        navigateToFolder(item.id)
                    }
                }
                chipGroup.addView(chip)

                // Add separator after each chip except the last one
                if (!isLastItem) {
                    // TODO: maybe add separator before each folder chip
                }
            }
        } else {
            // For shorter paths, show all folders
            for ((index, item) in path.withIndex()) {
                // TODO: maybe add separator before each folder chip

                val isLastItem = index == path.size - 1

                val chip = Chip(this).apply {
                    text = item.name
                    isCheckable = false
                    isClickable = true

                    // Style the chip
                    setChipBackgroundColorResource(R.color.chip_background)
                    setTextColor(ResourcesCompat.getColor(resources, R.color.chip_text, theme))
                    setRippleColorResource(R.color.chip_ripple)

                    // Highlight the current folder
                    if (isLastItem) {
                        setChipBackgroundColorResource(R.color.chip_selected_background)
                        setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.chip_selected_text,
                                theme
                            )
                        )
                    }

                    // Navigate to this folder when clicked
                    setOnClickListener {
                        navigateToFolder(item.id)
                    }
                }
                chipGroup.addView(chip)
            }
        }

        // Scroll to the end to show the current location
        binding.breadcrumbScrollView.post {
            binding.breadcrumbScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }
    }

    private fun showPathDropdownMenu(view: View, items: List<BreadcrumbItem>) {
        val popup = PopupMenu(this, view)

        // Add menu items for each folder in the truncated path
        items.forEachIndexed { index, item ->
            popup.menu.add(Menu.NONE, index, index, item.name)
        }

        // Handle menu item clicks
        popup.setOnMenuItemClickListener { menuItem ->
            val selectedItem = items[menuItem.itemId]
            navigateToFolder(selectedItem.id)
            true
        }

        popup.show()
    }

    private fun navigateToFolder(folderId: String?) {
        // If we're already at this folder, do nothing
        if (folderId == currentNodeId) return

        // If navigating to root from a subfolder
        if (folderId == null) {
            // Create a new intent for the root activity
            val intent = Intent(this, NodeActivity::class.java)
            // Clear the entire activity stack and start fresh
            //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            startActivity(intent)
        }else{
            // Otherwise, start a new NodeActivity with the selected folder as parent
            val intent = Intent(this, NodeActivity::class.java).apply {
                putExtra(EXTRA_PARENT_ID, folderId)
            }
            startActivity(intent)
        }

        finish()
    }

    private fun updateUI(nodes: List<Node>) {
        // If we're at root level show "Home", otherwise show the folder name
        if (currentNodeId == null) {
            supportActionBar?.title = getString(R.string.title_home)
            // Clear breadcrumb path when at root
            breadcrumbManager.clearPath()
            updateBreadcrumbUI(emptyList())
        } else {
            // Get the folder name using the parent ID
            viewModel.getNode(currentNodeId!!) { node ->
                supportActionBar?.title = node?.name ?: getString(R.string.folder)

                // Update breadcrumb path
                if (node != null) {
                    breadcrumbManager.navigateToFolder(node.id) { path ->
                        updateBreadcrumbUI(path)
                    }
                }
            }
        }

        nodeAdapter.updateNodes(nodes)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.node_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle back button press using the non-deprecated approach
                finish()
                true
            }

            R.id.action_settings -> {
                // Launch settings activity
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
} 
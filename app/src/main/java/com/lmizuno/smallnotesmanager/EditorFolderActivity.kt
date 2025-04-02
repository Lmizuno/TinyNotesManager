package com.lmizuno.smallnotesmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.lmizuno.smallnotesmanager.models.Folder
import com.lmizuno.smallnotesmanager.viewmodels.NodeViewModel

class EditorFolderActivity : AppCompatActivity() {
    private lateinit var name: TextInputEditText
    private lateinit var description: TextInputEditText
    private lateinit var doneButton: Button
    private lateinit var viewModel: NodeViewModel
    private var folder: Folder? = null
    private var parentId: String? = null

    companion object {
        private const val EXTRA_FOLDER_ID = "folder_id"
        private const val EXTRA_PARENT_ID = "parent_id"

        fun createIntent(
            context: Activity,
            folderId: String? = null,
            parentId: String? = null
        ): Intent {
            return Intent(context, EditorFolderActivity::class.java).apply {
                putExtra(EXTRA_FOLDER_ID, folderId)
                putExtra(EXTRA_PARENT_ID, parentId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_collection)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[NodeViewModel::class.java]

        // Get folder ID and parent ID from intent
        val folderId = intent.getStringExtra(EXTRA_FOLDER_ID)
        parentId = intent.getStringExtra(EXTRA_PARENT_ID)

        name = findViewById(R.id.nameInput)
        description = findViewById(R.id.descriptionInput)
        doneButton = findViewById(R.id.doneCollectionButton)

        // Setup observers
        setupObservers()

        // Load existing folder if we're editing
        if (folderId != null) {
            loadFolder(folderId)
        }

        doneButton.setOnClickListener {
            if (validateInputs()) {
                saveFolder()
            }
        }
    }

    private fun setupObservers() {
        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun loadFolder(folderId: String) {
        viewModel.getNode(folderId) { node ->
            if (node != null && node is Folder) {
                folder = node
                name.setText(folder?.name)
                description.setText(folder?.description)
            } else {
                Toast.makeText(this, "Failed to load folder", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (name.text.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.please_add_a_name), Toast.LENGTH_SHORT).show()
            return false
        }
        if (description.text.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.please_add_a_description), Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun saveFolder() {
        if (folder == null) {
            // Create new folder
            viewModel.createFolder(
                name = name.text.toString(),
                description = description.text.toString(),
                parentId = parentId
            ) { success ->
                if (success) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        } else {
            // Update existing folder
            folder?.apply {
                name = this@EditorFolderActivity.name.text.toString()
                description = this@EditorFolderActivity.description.text.toString()
                updatedAt = System.currentTimeMillis()
            }

            folder?.let { folder ->
                viewModel.updateNode(folder) { success ->
                    if (success) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }
}
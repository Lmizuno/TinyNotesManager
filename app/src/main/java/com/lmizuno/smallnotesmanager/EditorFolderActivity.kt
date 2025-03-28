package com.lmizuno.smallnotesmanager

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.lmizuno.smallnotesmanager.DBManager.CouchbaseManager
import com.lmizuno.smallnotesmanager.models.Folder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EditorFolderActivity : AppCompatActivity() {
    private lateinit var name: TextInputEditText
    private lateinit var description: TextInputEditText
    private lateinit var doneButton: Button
    private lateinit var dbManager: CouchbaseManager
    private var folder: Folder? = null
    private var parentId: String? = null

    companion object {
        private const val EXTRA_FOLDER_ID = "folder_id"
        private const val EXTRA_PARENT_ID = "parent_id"
        
        fun createIntent(context: Activity, folderId: String? = null, parentId: String? = null): Intent {
            return Intent(context, EditorFolderActivity::class.java).apply {
                putExtra(EXTRA_FOLDER_ID, folderId)
                putExtra(EXTRA_PARENT_ID, parentId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_collection)

        // Get folder ID and parent ID from intent
        val folderId = intent.getStringExtra(EXTRA_FOLDER_ID)
        parentId = intent.getStringExtra(EXTRA_PARENT_ID)
        name = findViewById(R.id.nameInput)
        description = findViewById(R.id.descriptionInput)
        doneButton = findViewById(R.id.doneCollectionButton)
        dbManager = CouchbaseManager.getInstance(this)

        // Load existing folder if we're editing
        if (folderId != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val doc = dbManager.getDocument(folderId)
                doc?.let {
                    folder = Folder(
                        id = it.getString("id") ?: "",
                        name = it.getString("name") ?: "",
                        parentId = it.getString("parent"),
                        createdAt = it.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = it.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                    name.setText(folder?.name)
                    description.setText(it.getString("description") ?: "")
                }
            }
        }

        doneButton.setOnClickListener {
            if (name.text.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.please_add_a_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (description.text.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.please_add_a_description), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveFolder()
        }
    }

    private fun saveFolder() {
        lifecycleScope.launch {
            try {
                val success = if (folder == null) {
                    // Create new folder
                    val newFolder = Folder(
                        id = UUID.randomUUID().toString(),
                        name = name.text.toString(),
                        parentId = parentId,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    Log.d("EditorFolder", "Attempting to save folder: ${newFolder.id}")
                    
                    val properties = newFolder.toMap().toMutableMap()
                    properties["description"] = description.text.toString()
                    properties["type"] = "Folder"  // Make sure type is set
                    
                    val savedDoc = dbManager.saveDocument(properties)
                    
                    // Verify the save and retrieve folder details
                    val verified = if (savedDoc != null) {
                        val verifyDoc = dbManager.getDocument(newFolder.id)
                        Log.d("EditorFolder", """
                            Verification details:
                            - ID: ${verifyDoc?.getString("id")}
                            - Name: ${verifyDoc?.getString("name")}
                            - Type: ${verifyDoc?.getString("type")}
                            - Parent: ${verifyDoc?.getString("parent")}
                            - Description: ${verifyDoc?.getString("description")}
                        """.trimIndent())
                        verifyDoc != null
                    } else {
                        Log.e("EditorFolder", "Initial save failed")
                        false
                    }
                    
                    verified
                } else {
                    // Update existing folder
                    val properties = folder!!.toMap().toMutableMap()
                    properties["name"] = name.text.toString()
                    properties["description"] = description.text.toString()
                    properties["updatedAt"] = System.currentTimeMillis()
                    properties["type"] = "Folder"  // Make sure type is set
                    
                    val updateSuccess = dbManager.updateDocument(folder!!.id, properties)
                    if (updateSuccess) {
                        val verifyDoc = dbManager.getDocument(folder!!.id)
                        Log.d("EditorFolder", """
                            Update verification details:
                            - ID: ${verifyDoc?.getString("id")}
                            - Name: ${verifyDoc?.getString("name")}
                            - Type: ${verifyDoc?.getString("type")}
                            - Parent: ${verifyDoc?.getString("parent")}
                            - Description: ${verifyDoc?.getString("description")}
                        """.trimIndent())
                    }
                    updateSuccess
                }

                if (success) {
                    Log.d("EditorFolder", "Operation completed successfully")
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Log.e("EditorFolder", "Operation failed")
                    Toast.makeText(this@EditorFolderActivity, getString(R.string.error_saving), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EditorFolder", "Exception during save", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditorFolderActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
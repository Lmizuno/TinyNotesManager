package com.lmizuno.smallnotesmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.lmizuno.smallnotesmanager.Models.Note
import com.lmizuno.smallnotesmanager.viewmodels.NodeViewModel
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher

class EditorNoteActivity : AppCompatActivity() {
    private lateinit var content: TextInputEditText
    private lateinit var title: TextInputEditText
    private lateinit var doneButton: Button
    private lateinit var removeButton: Button
    private lateinit var viewModel: NodeViewModel
    private var note: Note? = null
    private var parentId: String? = null

    companion object {
        private const val EXTRA_NOTE_ID = "note_id"
        private const val EXTRA_PARENT_ID = "parent_id"
        
        fun createIntent(context: Activity, noteId: String? = null, parentId: String? = null): Intent {
            return Intent(context, EditorNoteActivity::class.java).apply {
                putExtra(EXTRA_NOTE_ID, noteId)
                putExtra(EXTRA_PARENT_ID, parentId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_item)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[NodeViewModel::class.java]

        // Initialize views
        title = findViewById(R.id.titleInput)
        content = findViewById(R.id.contentInput)
        doneButton = findViewById(R.id.doneItemButton)
        removeButton = findViewById(R.id.removeItemButton)

        // Setup Markdown editor
        val markwon = Markwon.create(this)
        val editor = MarkwonEditor.create(markwon)
        content.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor))

        // Get note ID and parent ID from intent
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        parentId = intent.getStringExtra(EXTRA_PARENT_ID)

        // Setup observers
        setupObservers()

        // Load existing note if we're editing
        if (noteId != null) {
            loadNote(noteId)
        }

        doneButton.setOnClickListener {
            if (validateInputs()) {
                saveNote()
            }
        }

        removeButton.setOnClickListener {
            note?.let { currentNote ->
                viewModel.deleteNode(currentNote.id, parentId) { success ->
                    if (success) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
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

    private fun loadNote(noteId: String) {
        viewModel.getNode(noteId) { node ->
            if (node != null && node is Note) {
                note = node
                title.setText(note?.name)
                content.setText(note?.content)
                
                // Show remove button for existing notes
                removeButton.visibility = android.view.View.VISIBLE
            } else {
                Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (title.text.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.please_add_a_name), Toast.LENGTH_SHORT).show()
            return false
        }
        if (content.text.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.please_add_a_description), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveNote() {
        if (note == null) {
            // Create new note
            viewModel.createNote(
                name = title.text.toString(),
                content = content.text.toString(),
                parentId = parentId
            ) { success ->
                if (success) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        } else {
            // Update existing note
            note?.apply {
                name = this@EditorNoteActivity.title.text.toString()
                content = this@EditorNoteActivity.content.text.toString()
                updatedAt = System.currentTimeMillis()
            }
            
            note?.let { currentNote ->
                viewModel.updateNode(currentNote) { success ->
                    if (success) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }
}
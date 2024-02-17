package com.lmizuno.smallnotesmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Models.Item
import com.lmizuno.smallnotesmanager.Scripts.DeprecationManager
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher

class EditorItemActivity : AppCompatActivity() {
    private lateinit var content: TextInputEditText
    private lateinit var title: TextInputEditText
    private lateinit var doneButton: Button
    private lateinit var removeButton: Button
    private var item: Item? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_item)

        title = findViewById(R.id.titleInput)
        content = findViewById(R.id.contentInput)
        doneButton = findViewById(R.id.doneItemButton)
        removeButton = findViewById(R.id.removeItemButton)

        val markwon = Markwon.create(this)

        val editor = MarkwonEditor.create(markwon)
        //TODO: if slow change to      MarkwonEditorTextWatcher.withPreRender(editor,Executors.newCachedThreadPool(),editText)
        content.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor))

        //Set item to update or add
        var updateMode: Boolean = false
        if (intent != null && intent.hasExtra("intent")) {
            updateMode = (intent.getStringExtra("intent") != "add")
        }

        if (updateMode) {
            item = DeprecationManager().getSerializable(intent, "item", Item::class.java)

            title.setText(item!!.title)
            content.setText(item!!.content)

            removeButton.visibility = View.VISIBLE
        } else {
            removeButton.visibility = View.INVISIBLE
        }

        doneButton.setOnClickListener {
            if (title.text.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.please_add_a_name), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (content.text.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.please_add_a_description), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (item == null) {
                //If there was no item passed we create a new one
                item = Item(0, 0, title.text.toString(), content.text.toString())
            } else {
                //If there was, we update it
                item!!.title = title.text.toString()
                item!!.content = content.text.toString()
            }

            val intentReturn = Intent()
            intentReturn.putExtra("item", item)
            intentReturn.putExtra("intent", intent.getStringExtra("intent"))
            setResult(Activity.RESULT_OK, intentReturn)
            finish()
        }

        if (item != null) {
            removeButton.setOnClickListener {
                val builder = AlertDialog.Builder(this)

                builder.setMessage(getString(R.string.delete_question) + item!!.title)
                    .setPositiveButton(getString(R.string.remove)) { dialog, id ->
                        AppDatabase.getInstance(this).itemDao().delete(item!!)
                        finish()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, id ->
                    }
                builder.create().show()
            }
        }
    }
}
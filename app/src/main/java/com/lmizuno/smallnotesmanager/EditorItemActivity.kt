package com.lmizuno.smallnotesmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.lmizuno.smallnotesmanager.Models.Item
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher


class EditorItemActivity : AppCompatActivity() {
    private lateinit var content: TextInputEditText
    private lateinit var title: TextInputEditText
    private lateinit var doneButton: Button
    private var item: Item? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_item)

        title = findViewById(R.id.titleInput)
        content = findViewById(R.id.contentInput)
        doneButton = findViewById(R.id.doneItemButton)

        val markwon = Markwon.create(this)

        val editor = MarkwonEditor.create(markwon)
        //TODO: if slow change to      MarkwonEditorTextWatcher.withPreRender(editor,Executors.newCachedThreadPool(),editText)
        content.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor));

        //Set item to update if something was passed
        if (intent != null && intent.hasExtra("item")) {
            item = intent.getSerializableExtra("item", Item::class.java)

            if (item != null) {
                title.setText(item!!.title)
                content.setText(item!!.content)
            }
        }

        doneButton.setOnClickListener {
            if (title.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please add a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (content.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please add a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (item == null) {
                //If there was no item passed we create a new one
                item = Item(0, 0, title.text.toString(), content.text.toString())
            }else{
                //If there was, we update it
                item!!.title = title.text.toString()
                item!!.content = content.text.toString()
            }

            val intent = Intent()
            intent.putExtra("item", item)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
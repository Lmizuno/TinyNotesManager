package com.lmizuno.smallnotesmanager

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item

class NewItemActivity : AppCompatActivity() {
    private lateinit var content: TextInputEditText
    private lateinit var title: TextInputEditText
    private lateinit var add: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_item)


        title = findViewById(R.id.titleInput)
        content = findViewById(R.id.contentInput)
        add = findViewById(R.id.newItemButton)

        add.setOnClickListener {
            if (title.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please add a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (content.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please add a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = Item(0, 0, title.text.toString(), content.text.toString())

            val intent = Intent()
            intent.putExtra("item", item)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
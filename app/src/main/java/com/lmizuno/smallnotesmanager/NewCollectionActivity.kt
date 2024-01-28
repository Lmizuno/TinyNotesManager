package com.lmizuno.smallnotesmanager

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.lmizuno.smallnotesmanager.Models.Collection
class NewCollectionActivity : AppCompatActivity() {
    private lateinit var description: TextInputEditText
    private lateinit var name: TextInputEditText
    private lateinit var add: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_collection)

        name = findViewById(R.id.nameInput)
        description = findViewById(R.id.descriptionInput)
        add = findViewById(R.id.newCollectionButton)

        add.setOnClickListener {
            if (name.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please add a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (description.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please add a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val coll = Collection(0, name.text.toString(), description.text.toString())

            val intent = Intent()
            intent.putExtra("collection", coll)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
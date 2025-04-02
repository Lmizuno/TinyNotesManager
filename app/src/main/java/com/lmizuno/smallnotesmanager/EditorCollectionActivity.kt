package com.lmizuno.smallnotesmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.lmizuno.smallnotesmanager.models.Collection
import com.lmizuno.smallnotesmanager.scripts.DeprecationManager

class EditorCollectionActivity : AppCompatActivity() {
    private lateinit var description: TextInputEditText
    private lateinit var name: TextInputEditText
    private lateinit var doneButton: Button
    private var collection: Collection? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_collection)

        name = findViewById(R.id.nameInput)
        description = findViewById(R.id.descriptionInput)
        doneButton = findViewById(R.id.doneCollectionButton)

        var updateMode = false
        if (intent != null && intent.hasExtra("intent")) {
            updateMode = (intent.getStringExtra("intent") != "add")
        }

        if (updateMode) {
            collection =
                DeprecationManager().getSerializable(intent, "collection", Collection::class.java)

            name.setText(collection!!.name)
            description.setText(collection!!.description)
        }


        doneButton.setOnClickListener {
            if (name.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please add a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (description.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please add a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (collection == null) {
                collection = Collection(0, name.text.toString(), description.text.toString())
            } else {
                collection!!.name = name.text.toString()
                collection!!.description = description.text.toString()
            }

            val intentReturn = Intent()
            intentReturn.putExtra("collection", collection)
            intentReturn.putExtra("intent", intent.getStringExtra("intent"))
            setResult(Activity.RESULT_OK, intentReturn)
            finish()
        }
    }
}
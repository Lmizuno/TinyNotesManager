package com.lmizuno.smallnotesmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item

class CollectionPresentationActivity : AppCompatActivity() {
    private lateinit var collection: Collection
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_presentation)

        if (intent != null && intent.hasExtra("collection")) {
            collection = intent.getSerializableExtra("collection", Collection::class.java)!!

            db =
                AppDatabase.getInstance(baseContext) //TODO: this might create a mismatch between databases versions, analyse it
            val itemList: List<Item> =
                db.collectionDao().getCollectionItems(collection.collectionId)

        }
    }
}
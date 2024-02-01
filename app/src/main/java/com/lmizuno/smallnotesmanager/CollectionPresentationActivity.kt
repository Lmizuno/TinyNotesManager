package com.lmizuno.smallnotesmanager

import com.lmizuno.smallnotesmanager.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.lmizuno.smallnotesmanager.Adapters.PresentationAdapter
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item
import io.noties.markwon.Markwon
import me.relex.circleindicator.CircleIndicator3

class CollectionPresentationActivity : AppCompatActivity() {
    private lateinit var collection: Collection
    private lateinit var item: Item
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_presentation)

        if (intent != null && intent.hasExtra("collection")) {
            collection = intent.getSerializableExtra("collection", Collection::class.java)!!
            item = intent.getSerializableExtra("item", Item::class.java)!!

            db =
                AppDatabase.getInstance(baseContext) //TODO: this might create a mismatch between databases versions, analyse it
            val itemList: List<Item> =
                db.collectionDao().getCollectionItems(collection.collectionId)

            val markwon: Markwon = Markwon.create(this);

            val viewpager: ViewPager2 = findViewById(R.id.pageView)
            viewpager.adapter = PresentationAdapter(itemList, markwon)

            val indicator: CircleIndicator3 = findViewById(R.id.pageIndicator)
            indicator.setViewPager(viewpager)

            var currentItemIndex = 0
            itemList.forEachIndexed { index, element ->
                if (item.itemId == element.itemId) {
                    currentItemIndex = index
                }
            }

            viewpager.currentItem = currentItemIndex
        }
    }
}
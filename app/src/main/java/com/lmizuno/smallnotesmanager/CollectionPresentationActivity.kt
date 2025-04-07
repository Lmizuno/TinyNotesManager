package com.lmizuno.smallnotesmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.lmizuno.smallnotesmanager.adapters.PresentationAdapter
import com.lmizuno.smallnotesmanager.dbManager.AppDatabase
import com.lmizuno.smallnotesmanager.models.Collection
import com.lmizuno.smallnotesmanager.models.Item
import com.lmizuno.smallnotesmanager.scripts.DeprecationManager
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
            collection =
                DeprecationManager().getSerializable(intent, "collection", Collection::class.java)
            item = DeprecationManager().getSerializable(intent, "item", Item::class.java)

            db = AppDatabase.getInstance(baseContext)
            val itemList: List<Item> =
                db.collectionDao().getCollectionItems(collection.collectionId)

            val markwon: Markwon = Markwon.create(this)

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
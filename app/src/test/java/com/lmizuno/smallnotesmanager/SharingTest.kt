package com.lmizuno.smallnotesmanager

import android.content.Context
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lmizuno.smallnotesmanager.dbManager.AppDatabase
import com.lmizuno.smallnotesmanager.models.Collection
import com.lmizuno.smallnotesmanager.models.Item
import com.lmizuno.smallnotesmanager.scripts.Sharing
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SharingTest {
    private lateinit var db: AppDatabase
    private lateinit var sharing: Sharing
    private lateinit var collection: Collection
    private lateinit var item: Item
    private lateinit var realContext: Context

    @Before
    fun setup() {
        // Setup real database with real context
        realContext = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            realContext,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        // Create a collection to use in tests
        collection = Collection(name = "Test Collection", description = "Test Description")
        val collectionId = db.collectionDao().insert(collection)

        item = Item(
            collectionId = collectionId,
            title = "Test Item",
            content = "Test Content",
            orderN = 1
        )

        // Insert item and get generated ID
        db.itemDao().insert(item)
        
        sharing = Sharing()
    }
    
    @After
    fun tearDown() {
        db.close()
    }
    
    @Test
    fun `test full export and import cycle`() {
        // 1. Export to file
        val exportedFile = sharing.saveToFile(collection, realContext)
        assertTrue(exportedFile.exists())
        
        // 2. Delete data from database
        db.collectionDao().deleteCollectionAndItems(collection)
        
        // 3. Verify deletion
        assertNull(db.collectionDao().getCollectionById(collection.collectionId))
        assertNull(db.itemDao().getItemById(item.itemId))
        
        // 4. Import from file
        sharing.importFromFile(exportedFile.toUri(), realContext)
        
        // 5. Verify imported data matches original
        val importedCollections = db.collectionDao().getAll()
        assertEquals(1, importedCollections.size)
        
        val importedCollection = importedCollections[0]
        assertEquals(collection.name, importedCollection.name)
        assertEquals(collection.description, importedCollection.description)
        
        val importedItems = db.collectionDao().getCollectionItems(importedCollection.collectionId)
        assertEquals(1, importedItems.size)
        
        val importedItem = importedItems[0]
        assertEquals(item.title, importedItem.title)
        assertEquals(item.content, importedItem.content)
        assertEquals(item.orderN, importedItem.orderN)
    }
} 
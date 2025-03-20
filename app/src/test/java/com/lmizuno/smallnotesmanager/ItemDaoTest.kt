package com.lmizuno.smallnotesmanager

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ItemDaoTest {
    private lateinit var db: AppDatabase
    private var collectionId: Long = 0

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()

        // Create a collection to use in tests
        val collection = Collection(name = "Test Collection", description = "Test Description")
        collectionId = db.collectionDao().insert(collection)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadItem() {
        val item = Item(
            collectionId = collectionId, title = "Test Item", content = "Test Content", orderN = 1
        )

        // Insert item and get generated ID
        val id = db.itemDao().insert(item)

        // Read item from DB
        val loadedItem = db.itemDao().getItemById(id)

        // Verify item was stored correctly
        assertNotNull(loadedItem)
        assertEquals("Test Item", loadedItem?.title)
        assertEquals("Test Content", loadedItem?.content)
        assertEquals(1L, loadedItem?.orderN)
        assertEquals(collectionId, loadedItem?.collectionId)
    }

    @Test
    fun updateItem() {
        // Create and insert initial item
        val item = Item(
            collectionId = collectionId,
            title = "Initial Title",
            content = "Initial Content",
            orderN = 1
        )
        val id = db.itemDao().insert(item)

        // Update the item
        val updatedItem = Item(
            itemId = id,
            collectionId = collectionId,
            title = "Updated Title",
            content = "Updated Content",
            orderN = 2
        )
        val updateResult = db.itemDao().update(updatedItem)

        // Verify update was successful
        assertEquals(1, updateResult)

        // Read updated item
        val loadedItem = db.itemDao().getItemById(id)
        assertEquals("Updated Title", loadedItem?.title)
        assertEquals("Updated Content", loadedItem?.content)
        assertEquals(2L, loadedItem?.orderN)
    }

    @Test
    fun deleteItem() {
        // Create and insert item
        val item = Item(
            collectionId = collectionId, title = "To Delete", content = "Content", orderN = 1
        )
        val id = db.itemDao().insert(item)

        // Get the inserted item and delete it
        val itemToDelete = db.itemDao().getItemById(id)
        assertNotNull(itemToDelete)
        db.itemDao().delete(itemToDelete!!)

        // Verify item was deleted
        val deletedItem = db.itemDao().getItemById(id)
        assertNull(deletedItem)
    }
} 
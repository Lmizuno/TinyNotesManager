package com.lmizuno.smallnotesmanager

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Models.Collection
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CollectionDaoTest {
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadCollection() {
        val collection = Collection(
            name = "Test Collection", description = "Test Description"
        )

        // Insert collection and get generated ID
        val id = db.collectionDao().insert(collection)

        // Read collection from DB
        val loadedCollection = db.collectionDao().getCollectionById(id)

        // Verify collection was stored correctly
        assertNotNull(loadedCollection)
        assertEquals("Test Collection", loadedCollection?.name)
        assertEquals("Test Description", loadedCollection?.description)
    }
} 
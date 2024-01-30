package com.lmizuno.smallnotesmanager.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item

@Dao
interface CollectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(collection: Collection): Long

    @Query("SELECT * FROM collections WHERE collectionId = :collectionId")
    fun getCollectionById(collectionId: Int): Collection?

    @Query("SELECT * FROM collections")
    fun getAll(): List<Collection>

    @Query("SELECT * FROM items WHERE collectionId = :collectionId ORDER BY itemId ASC")
    fun getCollectionItems(collectionId: Int): List<Item>

    @Delete
    fun deleteCollectionAndItems(collection: Collection)
}
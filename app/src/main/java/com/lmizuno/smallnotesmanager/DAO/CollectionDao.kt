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
    fun getCollectionById(collectionId: Long): Collection?

    @Query("SELECT * FROM collections")
    fun getAll(): List<Collection>

    @Query("SELECT * FROM items WHERE collectionId = :collectionId ORDER BY orderN ASC")
    fun getCollectionItems(collectionId: Long): List<Item>

    @Query("SELECT COUNT(*) FROM items WHERE collectionId = :collectionId")
    fun getCollectionSize(collectionId: Long): Long

    @Delete
    fun deleteCollectionAndItems(collection: Collection)
}
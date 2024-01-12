package com.lmizuno.smallnotesmanager.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(item: Item): Long

    @Query("SELECT * FROM items WHERE itemId = :itemId")
    fun getItemById(itemId: Int): Item?

    @Query("SELECT * FROM items WHERE collectionId = :collectionId ORDER BY itemId ASC")
    fun getItemsByCollection(collectionId: Collection): List<Item>

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(item: Item): Long

    @Delete
    fun delete(itemId: Int)
}
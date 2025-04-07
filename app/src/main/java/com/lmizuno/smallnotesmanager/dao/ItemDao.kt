package com.lmizuno.smallnotesmanager.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lmizuno.smallnotesmanager.models.Item

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(item: Item): Long

    @Query("SELECT * FROM items WHERE itemId = :itemId")
    fun getItemById(itemId: Long): Item?

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(item: Item): Int

    @Delete
    fun delete(itemId: Item)
}
package com.lmizuno.smallnotesmanager.Models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "items",
    foreignKeys = [ForeignKey(
        entity = Collection::class,
        parentColumns = ["collectionId"],
        childColumns = ["collectionId"],
        onDelete = ForeignKey.CASCADE
    )])
data class Item(
    @PrimaryKey(autoGenerate = true)
    val itemId: Int,
    @ColumnInfo(index = true)
    val collectionId: Int,
    val title: String?,
    val content: String?
): Serializable

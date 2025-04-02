package com.lmizuno.smallnotesmanager.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "items", foreignKeys = [ForeignKey(
        entity = Collection::class,
        parentColumns = ["collectionId"],
        childColumns = ["collectionId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Item(
    @PrimaryKey(autoGenerate = true) var itemId: Long = 0,
    @ColumnInfo(index = true) var collectionId: Long = 0,
    var title: String = "",
    var content: String = "",
    @ColumnInfo(defaultValue = "1") var orderN: Long = 1
) : Serializable
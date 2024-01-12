package com.lmizuno.smallnotesmanager.Models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true)
    val collectionId: Int,
    val name: String?,
    val description: String?
)

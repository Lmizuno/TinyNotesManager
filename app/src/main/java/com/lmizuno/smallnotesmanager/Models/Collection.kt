package com.lmizuno.smallnotesmanager.Models
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true)
    val collectionId: Int = 0,
    val name: String?,
    val description: String?
):Serializable

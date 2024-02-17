package com.lmizuno.smallnotesmanager.Models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true)
    var collectionId: Long = 0,
    var name: String = "",
    var description: String = ""
) : Serializable
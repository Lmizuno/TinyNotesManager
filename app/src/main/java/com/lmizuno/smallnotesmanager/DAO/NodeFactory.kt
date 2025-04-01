package com.lmizuno.smallnotesmanager.DAO

import com.lmizuno.smallnotesmanager.Models.Folder
import com.lmizuno.smallnotesmanager.Models.Node
import com.lmizuno.smallnotesmanager.Models.NodeType
import com.lmizuno.smallnotesmanager.Models.Note
import android.util.Log

object NodeFactory {
    fun fromDocument(document: com.couchbase.lite.Document): Node? {
        try {
            // Convert document to map
            val map = document.toMap()
            
            // Get the type
            val typeString = map["type"] as? String ?: return null
            val type = try {
                NodeType.valueOf(typeString)
            } catch (e: IllegalArgumentException) {
                Log.e("NodeFactory", "Invalid node type: $typeString")
                return null
            }
            
            // Create the appropriate node type
            return when (type) {
                NodeType.FOLDER -> Folder.fromMap(map)
                NodeType.NOTE -> Note.fromMap(map)
                else -> null
            }
        } catch (e: Exception) {
            Log.e("NodeFactory", "Error creating node from document", e)
            return null
        }
    }
}
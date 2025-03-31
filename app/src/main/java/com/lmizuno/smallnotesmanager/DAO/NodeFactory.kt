package com.lmizuno.smallnotesmanager.DAO

import com.lmizuno.smallnotesmanager.Models.Folder
import com.lmizuno.smallnotesmanager.Models.Node
import com.lmizuno.smallnotesmanager.Models.NodeType
import com.lmizuno.smallnotesmanager.Models.Note
import android.util.Log

object NodeFactory {
    fun fromDocument(document: com.couchbase.lite.Document): Node? {
        val id = document.getString("id") ?: return null
        val name = document.getString("name") ?: return null
        val parentId = document.getString("parent").takeIf { it?.isNotEmpty() ?: false }
        val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
        val updatedAt = document.getLong("updatedAt") ?: System.currentTimeMillis()
        
        val typeString = document.getString("type") ?: return null
        val type = try {
            NodeType.valueOf(typeString)
        } catch (e: IllegalArgumentException) {
            Log.e("NodeFactory", "Invalid node type: $typeString")
            return null
        }

        return when (type) {
            NodeType.FOLDER -> Folder(id, name, parentId, createdAt, updatedAt)
            NodeType.NOTE -> {
                val content = document.getString("content") ?: ""
                Note(id, name, parentId, createdAt, updatedAt, content)
            }
            else -> null
        }
    }
}
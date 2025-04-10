package com.lmizuno.smallnotesmanager.models

import Attachment
import android.util.Log

class Note(
    id: String,
    name: String,
    parentId: String?,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis(),
    var content: String = "",
    var attachments: List<Attachment> = emptyList(),
    order: Long = 0
) : Node(id, name, parentId, createdAt, updatedAt, order, NodeType.NOTE) {
    override fun toMap(): Map<String, Any> {
        return mutableMapOf(
            "id" to id,
            "name" to name,
            "type" to type.name,
            "content" to content,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "order" to order,
            "attachments" to attachments.map { it.toMap() }
        ).apply {
            parentId?.let { this["parent"] = it }
        }
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Note {
            val attachmentsList = mutableListOf<Attachment>()

            // Parse attachments if they exist
            (map["attachments"] as? List<*>)?.forEach { item ->
                if (item is Map<*, *>) {
                    try {
                        @Suppress("UNCHECKED_CAST") val attachmentMap = item as Map<String, Any?>
                        attachmentsList.add(Attachment.fromMap(attachmentMap))
                    } catch (e: Exception) {
                        Log.e("Note", "Error parsing attachment", e)
                    }
                }
            }

            return Note(
                id = map["id"] as String,
                name = map["name"] as String,
                parentId = map["parent"] as? String,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis(),
                content = map["content"] as? String ?: "",
                attachments = attachmentsList,
                order = (map["order"] as? Long) ?: 0
            )
        }
    }
}

package com.lmizuno.smallnotesmanager.models

enum class ContentType {
    TEXT,
    IMAGE,
    PDF,
    LINK,
    BINARY
}

data class Attachment(
    val id: String,
    val type: ContentType,
    val filename: String,
    val size: Long,
    val mimeType: String,
    val thumbnailPath: String? = null,
    val externalPath: String? = null,
    val url: String? = null,
    val blobId: String? = null
)

abstract class Node(
    val id: String,
    var name: String,
    var parentId: String?,
    val createdAt: Long,
    var updatedAt: Long
) {
    abstract fun toMap(): Map<String, Any>
}

class Folder(
    id: String,
    name: String,
    parentId: String?,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis(),
    var description: String = ""
) : Node(id, name, parentId, createdAt, updatedAt) {
    override fun toMap(): Map<String, Any> {
        return mutableMapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "type" to "Folder",
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        ).apply {
            parentId?.let { this["parent"] = it }
        }
    }
}

class Note(
    id: String,
    name: String,
    parentId: String?,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis(),
    var content: String = "",
    var attachments: MutableList<Attachment> = mutableListOf()
) : Node(id, name, parentId, createdAt, updatedAt) {
    override fun toMap(): Map<String, Any> {
        return mutableMapOf(
            "id" to id,
            "name" to name,
            "type" to "Note",
            "content" to content,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "attachments" to attachments.map { attachment ->
                mapOf(
                    "id" to attachment.id,
                    "type" to attachment.type.name,
                    "filename" to attachment.filename,
                    "size" to attachment.size,
                    "mimeType" to attachment.mimeType,
                    "thumbnailPath" to (attachment.thumbnailPath ?: ""),
                    "externalPath" to (attachment.externalPath ?: ""),
                    "url" to (attachment.url ?: ""),
                    "blobId" to (attachment.blobId ?: "")
                )
            }
        ).apply {
            parentId?.let { this["parent"] = it }
        }
    }
}

object NodeFactory {
    fun fromDocument(document: com.couchbase.lite.Document): Node? {
        val id = document.getString("id") ?: return null
        val name = document.getString("name") ?: return null
        val parentId = document.getString("parent").takeIf { it?.isNotEmpty() ?: false }
        val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
        val updatedAt = document.getLong("updatedAt") ?: System.currentTimeMillis()
        val type = document.getString("type") ?: return null
        
        return when (type) {
            "Folder" -> Folder(id, name, parentId, createdAt, updatedAt)
            "Note" -> {
                val content = document.getString("content") ?: ""
                Note(id, name, parentId, createdAt, updatedAt, content)
            }
            else -> null
        }
    }
} 
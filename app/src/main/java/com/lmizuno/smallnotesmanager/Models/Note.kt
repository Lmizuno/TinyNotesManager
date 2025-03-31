package com.lmizuno.smallnotesmanager.Models

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
            "type" to NodeType.NOTE,
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

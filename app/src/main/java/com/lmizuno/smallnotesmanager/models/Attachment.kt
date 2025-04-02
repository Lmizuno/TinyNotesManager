enum class ContentType {
    TEXT, IMAGE, PDF, LINK, BINARY
}

class Attachment(
    val id: String,
    val type: ContentType,
    val filename: String,
    val size: Long,
    val mimeType: String,
    val thumbnailPath: String? = null,
    val externalPath: String? = null,
    val url: String? = null,
    val blobId: String? = null
) {
    fun toMap(): Map<String, Any> {
        return mutableMapOf(
            "id" to id,
            "type" to type.name,
            "filename" to filename,
            "size" to size,
            "mimeType" to mimeType
        ).apply {
            thumbnailPath?.let { this["thumbnailPath"] = it }
            externalPath?.let { this["externalPath"] = it }
            url?.let { this["url"] = it }
            blobId?.let { this["blobId"] = it }
        }
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Attachment {
            return Attachment(
                id = map["id"] as String,
                type = ContentType.valueOf(map["type"] as String),
                filename = map["filename"] as? String ?: "",
                size = map["size"] as? Long ?: 0L,
                mimeType = map["mimeType"] as? String ?: "",
                thumbnailPath = map["thumbnailPath"] as? String,
                externalPath = map["externalPath"] as? String,
                url = map["url"] as? String,
                blobId = map["blobId"] as? String
            )
        }
    }
} 
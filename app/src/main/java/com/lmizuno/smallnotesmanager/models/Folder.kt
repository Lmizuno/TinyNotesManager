package com.lmizuno.smallnotesmanager.models

class Folder(
    id: String,
    name: String,
    parentId: String?,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis(),
    var description: String = ""
) : Node(id, name, parentId, createdAt, updatedAt, NodeType.FOLDER) {
    override fun toMap(): Map<String, Any> {
        return mutableMapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "type" to type.name,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        ).apply {
            parentId?.let { this["parent"] = it }
        }
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Folder {
            return Folder(
                id = map["id"] as String,
                name = map["name"] as String,
                parentId = map["parent"] as? String,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis(),
                description = map["description"] as? String ?: ""
            )
        }
    }
}

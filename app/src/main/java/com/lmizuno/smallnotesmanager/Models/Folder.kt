package com.lmizuno.smallnotesmanager.Models

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
            "type" to NodeType.FOLDER,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        ).apply {
            parentId?.let { this["parent"] = it }
        }
    }
}

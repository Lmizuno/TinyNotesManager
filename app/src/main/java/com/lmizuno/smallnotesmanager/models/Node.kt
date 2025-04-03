package com.lmizuno.smallnotesmanager.models

enum class NodeType {
    FOLDER, NOTE
}

abstract class Node(
    val id: String,
    var name: String,
    var parentId: String?,
    val createdAt: Long,
    var updatedAt: Long,
    var order: Long,
    val type: NodeType
) {
    abstract fun toMap(): Map<String, Any>
}


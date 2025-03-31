package com.lmizuno.smallnotesmanager.Models

enum class NodeType {
    FOLDER,
    NOTE
}

abstract class Node(
    val id: String,
    var name: String,
    var parentId: String?,
    val createdAt: Long,
    var updatedAt: Long
) {
    abstract fun toMap(): Map<String, Any>
}


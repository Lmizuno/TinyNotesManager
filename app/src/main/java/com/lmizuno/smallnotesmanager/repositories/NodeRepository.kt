package com.lmizuno.smallnotesmanager.repositories

import android.content.Context
import android.util.Log
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.lmizuno.smallnotesmanager.dao.NodeFactory
import com.lmizuno.smallnotesmanager.dbManager.CouchbaseManager
import com.lmizuno.smallnotesmanager.models.Folder
import com.lmizuno.smallnotesmanager.models.Node
import com.lmizuno.smallnotesmanager.models.NodeType
import com.lmizuno.smallnotesmanager.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository for handling all node-related database operations.
 */
class NodeRepository(context: Context) {
    private val database: Database = CouchbaseManager.getInstance(context).getDatabase()

    companion object {
        private const val TAG = "NodeRepository"

        @Volatile
        private var instance: NodeRepository? = null

        fun getInstance(context: Context): NodeRepository {
            return instance ?: synchronized(this) {
                instance ?: NodeRepository(context).also { instance = it }
            }
        }
    }

    /**
     * Saves a new node document to the database.
     */
    suspend fun saveNode(node: Node): Boolean = withContext(Dispatchers.IO) {
        try {
            // Get the node type
            val type = when (node) {
                is Folder -> NodeType.FOLDER
                is Note -> NodeType.NOTE
                else -> throw IllegalArgumentException("Unsupported node type")
            }.name

            // Use pure ID without prefix
            val docId = node.id

            val properties = node.toMap().toMutableMap().apply {
                // Make sure type is set properly for storage
                this["type"] = type
            }

            val mutableDoc = MutableDocument(docId, properties)
            database.save(mutableDoc)

            Log.d(
                TAG, """
                Saved document:
                - ID: $docId
                - Type: $type
                - Name: ${node.name}
            """.trimIndent()
            )

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save node", e)
            false
        }
    }

    /**
     * Updates an existing node in the database.
     */
    suspend fun updateNode(node: Node): Boolean = withContext(Dispatchers.IO) {
        try {
            val type = when (node) {
                is Folder -> NodeType.FOLDER
                is Note -> NodeType.NOTE
                else -> throw IllegalArgumentException("Unsupported node type")
            }.name

            // Use pure ID without prefix
            val docId = node.id

            database.getDocument(docId)?.let { doc ->
                val mutableDoc = doc.toMutable()
                val properties = node.toMap()

                properties.forEach { (key, value) ->
                    mutableDoc.setValue(key, value)
                }

                // Ensure type is consistent
                mutableDoc.setValue("type", type)

                database.save(mutableDoc)
                Log.d(TAG, "Updated document: $docId")
                true
            } ?: run {
                Log.e(TAG, "Document not found for update: $docId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update node", e)
            false
        }
    }

    /**
     * Gets a node by its ID.
     */
    suspend fun getNode(id: String): Node? = withContext(Dispatchers.IO) {
        try {
            // Simply get document by its ID
            database.getDocument(id)?.let { doc ->
                NodeFactory.fromDocument(doc)
            } ?: run {
                Log.e(TAG, "Document not found for ID: $id")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get node: $id", e)
            null
        }
    }

    /**
     * Adds this method to get next available order for a parent
     */
    private suspend fun getNextOrder(parentId: String?): Long = withContext(Dispatchers.IO) {
        try {
            val nodes = queryNodesByParent(parentId)
            return@withContext if (nodes.isEmpty()) 0L else nodes.maxOf { it.order } + 1
        } catch (e: Exception) {
            Log.e(TAG, "Error getting next order", e)
            return@withContext 0L
        }
    }

    /**
     * Creates a new folder with the given details.
     */
    suspend fun createFolder(name: String, description: String, parentId: String?): Folder? =
        withContext(Dispatchers.IO) {
            try {
                val folderId = UUID.randomUUID().toString()
                val order = getNextOrder(parentId)
                val folder = Folder(
                    id = folderId,
                    name = name,
                    parentId = parentId,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    order = order
                )

                if (saveNode(folder)) {
                    folder
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create folder", e)
                null
            }
        }

    /**
     * Creates a new note with the given details.
     */
    suspend fun createNote(name: String, content: String, parentId: String?): Note? =
        withContext(Dispatchers.IO) {
            try {
                val noteId = UUID.randomUUID().toString()
                val order = getNextOrder(parentId)
                val note = Note(
                    id = noteId,
                    name = name,
                    parentId = parentId,
                    content = content,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    order = order
                )

                if (saveNode(note)) {
                    note
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create note", e)
                null
            }
        }

    /**
     * Queries nodes by parent ID.
     */
    suspend fun queryNodesByParent(parentId: String?): List<Node> = withContext(Dispatchers.IO) {
        try {
            val query = if (parentId == null) {
                QueryBuilder.select(SelectResult.all()).from(DataSource.database(database))
                    .where(Expression.property("parent").isNotValued())
            } else {
                QueryBuilder.select(SelectResult.all()).from(DataSource.database(database))
                    .where(Expression.property("parent").equalTo(Expression.string(parentId)))
            }

            Log.d(
                TAG, """
                Executing parent query:
                - Parent ID: ${parentId ?: "ROOT"}
                - Database size: ${database.count}
            """.trimIndent()
            )

            val nodes = query.execute().allResults().mapNotNull { result ->
                val docId = result.getDictionary(0)?.getString("id") ?: return@mapNotNull null

                database.getDocument(docId)?.let { doc ->
                    NodeFactory.fromDocument(doc)
                }
            }
            
            // Sort nodes by order
            return@withContext nodes.sortedBy { it.order }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query nodes by parent", e)
            emptyList()
        }
    }

    /**
     * Deletes a node by its ID.
     */
    suspend fun deleteNode(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Simply get and delete document by its ID
            database.getDocument(id)?.let { doc ->
                database.delete(doc)
                Log.d(TAG, "Deleted document: $id")
                true
            } ?: run {
                Log.e(TAG, "Document not found for deletion: $id")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete node: $id", e)
            false
        }
    }

    /**
     * Recursively queries all nodes under a parent ID
     */
    suspend fun queryNodesRecursively(parentId: String?): List<Node> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Node>()
        
        try {
            // Get direct children
            val directChildren = queryNodesByParent(parentId)

            // First process all subfolders recursively
            for (node in directChildren) {
                result.add(node)
                if (node.type == NodeType.FOLDER){
                    val childNodes = queryNodesRecursively(node.id)
                    result.addAll(childNodes)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying nodes recursively", e)
        }
        
        result
    }

    /**
     * Adds this method to update orders for a list of nodes
     */
    suspend fun updateNodesOrder(nodes: List<Node>): Boolean = withContext(Dispatchers.IO) {
        try {
            var success = true
            for (node in nodes) {
                node.updatedAt = System.currentTimeMillis()
                if (!updateNode(node)) {
                    success = false
                }
            }
            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Error updating nodes order", e)
            return@withContext false
        }
    }
} 
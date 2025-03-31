package com.lmizuno.smallnotesmanager.repositories

import android.content.Context
import com.couchbase.lite.*
import com.lmizuno.smallnotesmanager.DBManager.CouchbaseManager
import com.lmizuno.smallnotesmanager.Models.Folder
import com.lmizuno.smallnotesmanager.Models.Node
import com.lmizuno.smallnotesmanager.Models.NodeType
import com.lmizuno.smallnotesmanager.Models.Note
import com.lmizuno.smallnotesmanager.DAO.NodeFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
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
            // Create document ID with type prefix
            val type = when (node) {
                is Folder -> NodeType.FOLDER
                is Note -> NodeType.NOTE
                else -> throw IllegalArgumentException("Unsupported node type")
            }.name
            
            val docId = "$type::${node.id}"
            
            val properties = node.toMap().toMutableMap().apply {
                // Make sure type is set properly for storage
                this["type"] = type
            }
            
            val mutableDoc = MutableDocument(docId, properties)
            database.save(mutableDoc)
            
            Log.d(TAG, """
                Saved document:
                - ID: $docId
                - Type: $type
                - Name: ${node.name}
            """.trimIndent())
            
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
            
            val docId = "$type::${node.id}"
            
            database.getDocument(docId)?.let { doc ->
                val mutableDoc = doc.toMutable()
                val properties = node.toMap()
                
                properties.forEach { (key, value) -> 
                    mutableDoc.setValue(key, value)
                }
                
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
            // We need to try both document ID formats since we don't know the type
            val folderDocId = "${NodeType.FOLDER.name}::$id"
            val noteDocId = "${NodeType.NOTE.name}::$id"
            
            // Try to get folder document first
            database.getDocument(folderDocId)?.let { doc ->
                return@withContext NodeFactory.fromDocument(doc)
            }
            
            // If not found, try note document
            database.getDocument(noteDocId)?.let { doc ->
                return@withContext NodeFactory.fromDocument(doc)
            }
            
            // If we get here, document wasn't found
            Log.e(TAG, "Document not found for ID: $id")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get node: $id", e)
            null
        }
    }

    /**
     * Creates a new folder with the given details.
     */
    suspend fun createFolder(name: String, description: String, parentId: String?): Folder? = withContext(Dispatchers.IO) {
        try {
            val folderId = UUID.randomUUID().toString()
            val folder = Folder(
                id = folderId,
                name = name,
                parentId = parentId,
                description = description,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
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
    suspend fun createNote(name: String, content: String, parentId: String?): Note? = withContext(Dispatchers.IO) {
        try {
            val noteId = UUID.randomUUID().toString()
            val note = Note(
                id = noteId,
                name = name,
                parentId = parentId,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
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
                QueryBuilder
                    .select(SelectResult.all())
                    .from(DataSource.database(database))
                    .where(Expression.property("parent").isNotValued())
            } else {
                QueryBuilder
                    .select(SelectResult.all())
                    .from(DataSource.database(database))
                    .where(Expression.property("parent").equalTo(Expression.string(parentId)))
            }

            Log.d(TAG, """
                Executing parent query:
                - Parent ID: ${parentId ?: "ROOT"}
                - Database size: ${database.count}
            """.trimIndent())

            query.execute().allResults().mapNotNull { result ->
                val docId = result.getDictionary(0)?.getString("id") ?: return@mapNotNull null
                
                // Determine the document type to construct the full ID
                val typeStr = result.getDictionary(0)?.getString("type") ?: return@mapNotNull null
                val fullDocId = "$typeStr::$docId"
                
                database.getDocument(fullDocId)?.let { doc ->
                    NodeFactory.fromDocument(doc)
                }
            }.also { nodes ->
                Log.d(TAG, "Query returned ${nodes.size} nodes")
            }
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
            database.getDocument(id)?.let { doc ->
                database.delete(doc)
                Log.d(TAG, "Deleted document: $id")
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete node: $id", e)
            false
        }
    }
} 
package com.lmizuno.smallnotesmanager.DBManager

import android.content.Context
import com.couchbase.lite.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * Author: Luis Henrique Mizuno
 * Date: 2025-03-27
 */
class CouchbaseManager private constructor(context: Context) {
    private val database: Database
    
    init {
        try {
            CouchbaseLite.init(context)
            
            // Create database configuration with explicit path
            val dbConfig = DatabaseConfigurationFactory.create(context.filesDir.toString())
            val dbName = "notes_db"
            
            database = if (Database.exists(dbName, context.filesDir)) {
                Log.i("CouchbaseManager", "Opening existing database: $dbName")
                Database(dbName, dbConfig)
            } else {
                Log.i("CouchbaseManager", "Creating new database: $dbName")
                Database(dbName, dbConfig)
            }
            
            Log.i("CouchbaseManager", """
                Database initialized:
                - Name: ${database.name}
                - Path: ${database.path}
                - Document count: ${database.count}
            """.trimIndent())
            
        } catch (e: Exception) {
            Log.e("CouchbaseManager", "Failed to initialize database", e)
            throw RuntimeException("Database initialization failed", e)
        }
    }
    
    companion object {
        @Volatile
        private var instance: CouchbaseManager? = null
        
        fun getInstance(context: Context): CouchbaseManager {
            return instance ?: synchronized(this) {
                instance ?: CouchbaseManager(context).also { instance = it }
            }
        }
    }

    suspend fun saveDocument(properties: Map<String, Any>): Document? = withContext(Dispatchers.IO) {
        try {
            // Create document ID with type prefix
            val type = properties["type"] as String
            val id = properties["id"] as String
            val docId = "$type::$id"
            
            val mutableDoc = MutableDocument(docId, properties)
            database.save(mutableDoc)
            
            Log.d("CouchbaseManager", """
                Saved document:
                - ID: $docId
                - Type: $type
                - Name: ${properties["name"]}
            """.trimIndent())
            
            database.getDocument(docId)
        } catch (e: Exception) {
            Log.e("CouchbaseManager", "Failed to save document", e)
            null
        }
    }

    suspend fun getDocument(id: String): Document? = withContext(Dispatchers.IO) {
        try {
            database.getDocument(id)?.also {
                Log.d("CouchbaseManager", "Retrieved document: $id")
            }
        } catch (e: Exception) {
            Log.e("CouchbaseManager", "Failed to get document: $id", e)
            null
        }
    }

    suspend fun updateDocument(id: String, properties: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        try {
            database.getDocument(id)?.let { doc ->
                val mutableDoc = doc.toMutable()
                properties.forEach { (key, value) -> mutableDoc.setValue(key, value) }
                database.save(mutableDoc)
                Log.d("CouchbaseManager", "Updated document: $id")
                true
            } ?: false
        } catch (e: Exception) {
            Log.e("CouchbaseManager", "Failed to update document: $id", e)
            false
        }
    }

    suspend fun queryByParent(parentId: String?): List<Document> = withContext(Dispatchers.IO) {
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

            Log.d("CouchbaseManager", """
                Executing parent query:
                - Parent ID: ${parentId ?: "ROOT"}
                - Database size: ${database.count}
            """.trimIndent())

            query.execute().allResults().mapNotNull { result ->
                result.getDictionary(0)?.getString("id")?.let { id ->
                    database.getDocument(id)
                }
            }.also { docs ->
                Log.d("CouchbaseManager", "Query returned ${docs.size} documents")
            }
        } catch (e: Exception) {
            Log.e("CouchbaseManager", "Failed to query documents", e)
            emptyList()
        }
    }

    fun close() {
        try {
            database.close()
            Log.i("CouchbaseManager", "Database closed successfully")
        } catch (e: Exception) {
            Log.e("CouchbaseManager", "Failed to close database", e)
        }
    }
} 
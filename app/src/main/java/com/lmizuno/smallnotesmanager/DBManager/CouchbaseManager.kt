package com.lmizuno.smallnotesmanager.DBManager

import android.content.Context
import com.couchbase.lite.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class CouchbaseManager private constructor(context: Context) {
    private val database: Database
    
    init {
        CouchbaseLite.init(context)
        val config = DatabaseConfiguration()
        database = Database("notes_db", config)
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
            val mutableDoc = MutableDocument(properties["id"] as String, properties)
            database.save(mutableDoc)
            database.getDocument(mutableDoc.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getDocument(id: String): Document? = withContext(Dispatchers.IO) {
        try {
            database.getDocument(id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateDocument(id: String, properties: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        try {
            database.getDocument(id)?.let { doc ->
                val mutableDoc = doc.toMutable()
                properties.forEach { (key, value) -> mutableDoc.setValue(key, value) }
                database.save(mutableDoc)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun queryByParent(parentId: String?): List<Document> = withContext(Dispatchers.IO) {
        try {
            val query = if (parentId == null) {
                QueryBuilder
                    .select(SelectResult.all())
                    .from(DataSource.database(database))
                    .where(Expression.property("parent").equalTo(Expression.value(null)))
            } else {
                QueryBuilder
                    .select(SelectResult.all())
                    .from(DataSource.database(database))
                    .where(Expression.property("parent").equalTo(Expression.string(parentId)))
            }
            
            query.execute().allResults().mapNotNull { result ->
                result.getDictionary(0)?.getString("id")?.let { id ->
                    database.getDocument(id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
} 
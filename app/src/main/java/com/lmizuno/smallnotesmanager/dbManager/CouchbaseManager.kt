package com.lmizuno.smallnotesmanager.dbManager

import android.content.Context
import android.util.Log
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfigurationFactory
import com.couchbase.lite.create

/**
 * Author: Luis Henrique Mizuno
 * Date: 2025-03-27
 *
 * Handles database initialization and provides access to the database instance.
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

            Log.i(
                "CouchbaseManager", """
                Database initialized:
                - Name: ${database.name}
                - Path: ${database.path}
                - Document count: ${database.count}
            """.trimIndent()
            )

        } catch (e: Exception) {
            Log.e("CouchbaseManager", "Failed to initialize database", e)
            throw RuntimeException("Database initialization failed", e)
        }
    }

    companion object {
        private const val TAG = "CouchbaseManager"

        @Volatile
        private var instance: CouchbaseManager? = null

        fun getInstance(context: Context): CouchbaseManager {
            return instance ?: synchronized(this) {
                instance ?: CouchbaseManager(context).also { instance = it }
            }
        }
    }

    fun getDatabase(): Database = database

    fun close() {
        try {
            database.close()
            Log.i("CouchbaseManager", "Database closed successfully")
        } catch (e: Exception) {
            Log.e("CouchbaseManager", "Failed to close database", e)
        }
    }
} 
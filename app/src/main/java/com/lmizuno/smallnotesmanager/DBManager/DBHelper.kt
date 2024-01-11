package com.lmizuno.smallnotesmanager.DBManager
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class MyDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Database name and version
        private const val DATABASE_NAME = "mycollection.db"
        private const val DATABASE_VERSION = 1

        // Table and column names
        private const val TABLE_NAME_COLLECTION = "collections"
        private const val COLUMN_ID_COLLECTION = "_id"
        private const val COLUMN_NAME_COLLECTION = "name"

        private const val TABLE_NAME_ITEM = "items"
        private const val COLUMN_ID_ITEM = "_id"
        private const val COLUMN_COLLECTION_ITEM = "collection_id"
        private const val COLUMN_NAME_ITEM = "title"
        private const val COLUMN_CONTENT_ITEM = "content"

        // SQL statement to create the table
        private const val SQL_CREATE_TABLE_COLLECTION =
            "CREATE TABLE $TABLE_NAME_COLLECTION (" +
                    "$COLUMN_ID_COLLECTION INTEGER PRIMARY KEY," +
                    "$COLUMN_NAME_COLLECTION TEXT)"
        private const val SQL_CREATE_TABLE_ITEM =
            "CREATE TABLE $TABLE_NAME_ITEM (" +
                    "$COLUMN_ID_ITEM INTEGER PRIMARY KEY," +
                    "$COLUMN_NAME_ITEM TEXT," +
                    "$COLUMN_CONTENT_ITEM TEXT," +
                    "$COLUMN_COLLECTION_ITEM INTEGER," +
                    "FOREIGN KEY($COLUMN_COLLECTION_ITEM) " +
                    "REFERENCES $TABLE_NAME_COLLECTION($COLUMN_ID_COLLECTION))"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE_COLLECTION)
        db.execSQL(SQL_CREATE_TABLE_ITEM)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Implement database upgrade logic here
    }
}

/**
// Insert a new collection
val collectionValues = ContentValues().apply {
put("name", "")
}

val collectionId = db.insert("collections", null, collectionValues)

// Insert a new item for the collection
val itemValues = ContentValues().apply {
put("collection_id", collectionId)
put("title", "Title Comes Here")
put("content", "May be a large content")
}

val itemId = db.insert("items", null, itemValues)


// Query all users and their orders
val query = """
SELECT items.title, users.email, orders.product_name, orders.order_date
FROM users
LEFT JOIN orders ON users.user_id = orders.user_id
""".trimIndent()

val cursor = db.rawQuery(query, null)

while (cursor.moveToNext()) {
val username = cursor.getString(cursor.getColumnIndex("username"))
val email = cursor.getString(cursor.getColumnIndex("email"))
val productName = cursor.getString(cursor.getColumnIndex("product_name"))
val orderDate = cursor.getString(cursor.getColumnIndex("order_date"))

// Process the data as needed
// For example, display it in the UI or log it
}

cursor.close()
 */

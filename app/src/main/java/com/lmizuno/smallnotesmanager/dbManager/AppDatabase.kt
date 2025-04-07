package com.lmizuno.smallnotesmanager.dbManager

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lmizuno.smallnotesmanager.dao.CollectionDao
import com.lmizuno.smallnotesmanager.dao.ItemDao
import com.lmizuno.smallnotesmanager.models.Collection
import com.lmizuno.smallnotesmanager.models.Item

@Database(
    entities = [Collection::class, Item::class], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun itemDao(): ItemDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): AppDatabase {
            if (instance == null) instance = Room.databaseBuilder(
                ctx.applicationContext, AppDatabase::class.java, "smallNotesManager"
            ).allowMainThreadQueries()
                //.addMigrations(MIGRATION_3_4)
                .build()

            return instance!!
        }
    }
}

//val MIGRATION_3_4: Migration = object : Migration(3, 4) {
//    override fun migrate(db: SupportSQLiteDatabase) {
//        db.beginTransaction()
//        try {
//            db.execSQL("ALTER TABLE users ADD COLUMN order TEXT NOT NULL DEFAULT `7FFFFFFFFFFF` ")
//            db.setTransactionSuccessful()
//        } finally {
//            db.endTransaction()
//        }
//    }
//}
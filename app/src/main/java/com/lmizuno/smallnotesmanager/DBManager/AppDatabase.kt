package com.lmizuno.smallnotesmanager.DBManager

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lmizuno.smallnotesmanager.DAO.CollectionDao
import com.lmizuno.smallnotesmanager.DAO.ItemDao
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item

@Database(
    entities = [Collection::class, Item::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ],
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun itemDao(): ItemDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): AppDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, AppDatabase::class.java,
                    "smallNotesManager"
                )
                    .allowMainThreadQueries()
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
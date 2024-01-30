package com.lmizuno.smallnotesmanager.DBManager

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lmizuno.smallnotesmanager.DAO.CollectionDao
import com.lmizuno.smallnotesmanager.DAO.ItemDao
import com.lmizuno.smallnotesmanager.Models.Item
import com.lmizuno.smallnotesmanager.Models.Collection

@Database(
    entities = [Collection::class, Item::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
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
                    .fallbackToDestructiveMigration()
                    .build()

            return instance!!
        }
    }
}
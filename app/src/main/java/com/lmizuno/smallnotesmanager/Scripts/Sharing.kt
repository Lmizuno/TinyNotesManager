package com.lmizuno.smallnotesmanager.Scripts

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item
import com.lmizuno.smallnotesmanager.Models.SharingData
import org.apache.commons.io.FilenameUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader

class Sharing {
    @SuppressLint("SetWorldReadable", "SetWorldWritable")
    fun saveToFile(collection: Collection, context: Context): File? {
        val downloadsPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val newFile = File(
            downloadsPath,
            "binder_${FilenameUtils.normalize(collection.name.lowercase())}.json"
        )

        val items: List<Item> = AppDatabase.getInstance(context).collectionDao()
            .getCollectionItems(collection.collectionId)

        try {
            FileWriter(newFile).use { writer ->
                val sharingObj = SharingData(collection, items)
                val json: String = Gson().toJson(sharingObj)

                writer.appendLine(json)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        newFile.setReadable(true, false)
        newFile.setWritable(true, false)
        newFile.setExecutable(true, false)

        return newFile
    }

    fun importFromFile(path: Uri, context: Context) {
        val inputStream = context.contentResolver.openInputStream(path)

        try {
            val db = AppDatabase.getInstance(context)

            val fileContent = inputStream?.bufferedReader().use { it?.readText() }
            val sharingData = Gson().fromJson(fileContent, SharingData::class.java)

            val newCollection =
                Collection(0, sharingData.collection.name, sharingData.collection.description)
            val colID = db.collectionDao().insert(newCollection)

            sharingData.items.forEach {
                val newItem = Item(0, colID, it.title, it.content)
                db.itemDao().insert(newItem)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
    }
}
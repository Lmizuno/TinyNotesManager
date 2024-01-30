package com.lmizuno.smallnotesmanager.Scripts

import android.content.Context
import android.os.Environment
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileWriter
import java.io.IOException

class Sharing {

    //TODO: Test this
    fun saveToFile(collection: Collection, context: Context): File? {
        val downloadsPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        //val sharingPath = File(context.cacheDir, "smallNotesManager")
        val newFile = File(downloadsPath, "binder_${FilenameUtils.normalize(collection.name)}.smn")

        val items: List<Item> = AppDatabase.getInstance(context).collectionDao()
            .getCollectionItems(collection.collectionId)

        //Basic and dumb export, we'll figure it out :)
        try {
            FileWriter(newFile).use { writer ->
                writer.appendLine("//born")

                writer.appendLine("name=${collection.name}")
                writer.appendLine("description=${collection.description}")

                items.forEach {
                    writer.appendLine("//begin")
                    writer.appendLine("title=${it.title}")
                    writer.appendLine("content=${it.content}")
                    writer.appendLine("//end")
                }

                writer.appendLine("//dead")
            }

            //this might show that dialog to share the file
            return newFile
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    //TODO: receive context and use DAO to insert whatever new thing we get
    fun ImportFromFile(path: String, context: Context) {
        val db = AppDatabase.getInstance(context)

    }
}
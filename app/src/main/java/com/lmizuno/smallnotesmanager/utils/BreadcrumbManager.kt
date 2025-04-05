package com.lmizuno.smallnotesmanager.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lmizuno.smallnotesmanager.models.BreadcrumbItem

class BreadcrumbManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "breadcrumb_prefs"
        private const val KEY_CURRENT_PATH = "current_breadcrumb_path"

        @Volatile
        private var instance: BreadcrumbManager? = null

        fun getInstance(context: Context): BreadcrumbManager {
            return instance ?: synchronized(this) {
                instance ?: BreadcrumbManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // Get the current breadcrumb path
    fun getCurrentPath(): List<BreadcrumbItem> {
        val json = prefs.getString(KEY_CURRENT_PATH, null) ?: return emptyList()
        val type = object : TypeToken<List<BreadcrumbItem>>() {}.type
        return gson.fromJson(json, type)
    }

    // Save the current breadcrumb path
    fun savePath(path: List<BreadcrumbItem>) {
        val json = gson.toJson(path)
        prefs.edit().putString(KEY_CURRENT_PATH, json).apply()
    }

    // Update the path when navigating to a folder
    fun navigateToFolder(folderId: String?, folderName: String): List<BreadcrumbItem> {
        val currentPath = getCurrentPath().toMutableList()

        // If navigating to root, clear the path
        if (folderId == null) {
            currentPath.clear()
        } else {
            // Check if we're already in the path (navigating up)
            val existingIndex = currentPath.indexOfFirst { it.id == folderId }
            if (existingIndex != -1) {
                // Truncate the path at this point
                while (currentPath.size > existingIndex + 1) {
                    currentPath.removeAt(currentPath.size - 1)
                }
            } else {
                // Add the new folder to the path
                currentPath.add(BreadcrumbItem(folderId, folderName))
            }
        }

        savePath(currentPath)
        return currentPath
    }

    // Clear the breadcrumb path
    fun clearPath() {
        prefs.edit().remove(KEY_CURRENT_PATH).apply()
    }
}
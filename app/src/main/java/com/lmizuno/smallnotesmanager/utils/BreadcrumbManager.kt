package com.lmizuno.smallnotesmanager.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lmizuno.smallnotesmanager.models.BreadcrumbItem
import com.lmizuno.smallnotesmanager.repositories.NodeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.edit

class BreadcrumbManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val nodeRepository = NodeRepository.getInstance(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val TAG = "BreadcrumbManager"
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
    private fun savePath(path: List<BreadcrumbItem>) {
        val json = gson.toJson(path)
        prefs.edit { putString(KEY_CURRENT_PATH, json) }
    }

    // Build the full path for a folder
    private fun buildFullPath(folderId: String?, callback: (List<BreadcrumbItem>) -> Unit) {
        if (folderId == null) {
            callback(emptyList())
            return
        }
        
        coroutineScope.launch {
            try {
                val path = mutableListOf<BreadcrumbItem>()
                var currentId = folderId
                
                // Build path from current folder up to root
                while (currentId != null) {
                    val node = withContext(Dispatchers.IO) {
                        nodeRepository.getNode(currentId!!)
                    }
                    
                    if (node != null) {
                        // Add to beginning of list (we're working backwards from current to root)
                        path.add(0, BreadcrumbItem(node.id, node.name))
                        currentId = node.parentId
                    } else {
                        // Node not found, break the loop
                        break
                    }
                }
                
                // Save the path and return it
                savePath(path)
                callback(path)
            } catch (e: Exception) {
                Log.e(TAG, "Error building folder path", e)
                callback(emptyList())
            }
        }
    }

    // Overloaded method with callback for async usage
    fun navigateToFolder(folderId: String?, callback: (List<BreadcrumbItem>) -> Unit) {
        if (folderId == null) {
            clearPath()
            callback(emptyList())
            return
        }
        
        // Build the full path for this folder
        buildFullPath(folderId) { path ->
            callback(path)
        }
    }

    // Clear the breadcrumb path
    fun clearPath() {
        prefs.edit { remove(KEY_CURRENT_PATH) }
    }
}
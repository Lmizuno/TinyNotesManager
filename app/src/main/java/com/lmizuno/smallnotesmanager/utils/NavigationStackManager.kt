package com.lmizuno.smallnotesmanager.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

/**
 * Manages a navigation stack of folder IDs to track user navigation history
 */
class NavigationStackManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val TAG = "NavigationStackManager"
        private const val PREFS_NAME = "navigation_stack_prefs"
        private const val KEY_NAVIGATION_STACK = "navigation_stack"
        
        @Volatile
        private var instance: NavigationStackManager? = null
        
        fun getInstance(context: Context): NavigationStackManager {
            return instance ?: synchronized(this) {
                instance ?: NavigationStackManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Get the current navigation stack
     * @return List of folder IDs representing the navigation history
     */
    fun getNavigationStack(): List<String?> {
        val json = prefs.getString(KEY_NAVIGATION_STACK, null) ?: return emptyList()
        val type = object : TypeToken<List<String?>>() {}.type
        return gson.fromJson(json, type)
    }
    
    /**
     * Save the navigation stack
     * @param stack List of folder IDs to save
     */
    private fun saveNavigationStack(stack: List<String?>) {
        val json = gson.toJson(stack)
        prefs.edit { putString(KEY_NAVIGATION_STACK, json) }
        Log.d(TAG, "Navigation stack updated: $stack")
    }
    
    /**
     * Push a folder ID to the top of the navigation stack
     * @param folderId The folder ID to push (null represents root)
     */
    fun pushFolder(folderId: String?) {
        val stack = getNavigationStack().toMutableList()
        
        // If the folder is already at the top of the stack, do nothing
        if (stack.isNotEmpty() && stack.last() == folderId) {
            return
        }
        
        // Add the folder to the stack
        stack.add(folderId)
        saveNavigationStack(stack)
    }
    
    /**
     * Pop the top folder ID from the navigation stack
     * @return The folder ID that was popped, or null if the stack was empty
     */
    fun popFolder(): String? {
        val stack = getNavigationStack().toMutableList()
        if (stack.isEmpty()) {
            return null
        }
        
        val poppedId = stack.removeAt(stack.size - 1)
        saveNavigationStack(stack)
        return poppedId
    }
    
    /**
     * Peek at the top folder ID without removing it
     * @return The top folder ID, or null if the stack is empty
     */
    fun peekFolder(): String? {
        val stack = getNavigationStack()
        return if (stack.isEmpty()) null else stack.last()
    }
    
    /**
     * Truncate the stack up to and including the specified folder ID
     * @param folderId The folder ID to truncate to
     * @return True if the folder was found and the stack was truncated, false otherwise
     */
    fun truncateToFolder(folderId: String?): Boolean {
        val stack = getNavigationStack().toMutableList()
        val index = stack.indexOf(folderId)
        
        if (index == -1) {
            Log.w(TAG, "Folder ID not found in stack: $folderId")
            return false
        }
        
        // Keep elements up to and excluding the specified folder ID
        val truncatedStack = stack.subList(0, index)
        saveNavigationStack(truncatedStack)
        return true
    }
    
    /**
     * Clear the navigation stack
     */
    fun clearStack() {
        prefs.edit { remove(KEY_NAVIGATION_STACK) }
        Log.d(TAG, "Navigation stack cleared")
    }
    
    /**
     * Get the position of a folder ID in the stack
     * @param folderId The folder ID to find
     * @return The position (0-based) of the folder ID, or -1 if not found
     */
    fun getFolderPosition(folderId: String?): Int {
        return getNavigationStack().indexOf(folderId)
    }
    
    /**
     * Check if the navigation stack contains a specific folder ID
     * @param folderId The folder ID to check
     * @return True if the stack contains the folder ID, false otherwise
     */
    fun containsFolder(folderId: String?): Boolean {
        return getFolderPosition(folderId) != -1
    }
}

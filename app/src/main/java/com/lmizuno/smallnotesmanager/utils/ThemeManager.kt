package com.lmizuno.smallnotesmanager.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class ThemeManager(private val context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun applyTheme() {
        // Check for the new theme preference first
        val themeValue = prefs.getString("app_theme", "system")
        
        when (themeValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> {
                // For backward compatibility with old preference
                val isDarkMode = prefs.getBoolean("dark_mode", false)
                if (isDarkMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }
    
    fun setTheme(themeValue: String) {
        prefs.edit().putString("app_theme", themeValue).apply()
    }

    companion object {
        fun getInstance(context: Context): ThemeManager {
            return ThemeManager(context.applicationContext)
        }
    }
} 
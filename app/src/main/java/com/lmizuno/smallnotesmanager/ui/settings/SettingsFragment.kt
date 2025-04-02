package com.lmizuno.smallnotesmanager.ui.settings

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.lmizuno.smallnotesmanager.R
import com.lmizuno.smallnotesmanager.utils.ThemeManager

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val darkModePreference = findPreference<SwitchPreference>("dark_mode")
        darkModePreference?.setOnPreferenceChangeListener { _, newValue ->
            val isDarkMode = newValue as Boolean
            // Apply theme
            ThemeManager.getInstance(requireContext()).applyTheme()

            // Recreate the activity to apply theme changes immediately
            activity?.recreate()

            true
        }

        // Set app version dynamically
        val versionPreference = findPreference<Preference>("app_version")
        try {
            val packageInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName
            versionPreference?.summary = versionName
        } catch (e: PackageManager.NameNotFoundException) {
            versionPreference?.summary = "Unknown"
        }
    }
} 
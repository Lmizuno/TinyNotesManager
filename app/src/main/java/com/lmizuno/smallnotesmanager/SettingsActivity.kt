package com.lmizuno.smallnotesmanager

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.lmizuno.smallnotesmanager.databinding.ActivitySettingsBinding
import com.lmizuno.smallnotesmanager.utils.ThemeManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            // Set up theme preference
            val themePreference = findPreference<ListPreference>("app_theme")
            themePreference?.setOnPreferenceChangeListener { _, newValue ->
                val themeValue = newValue as String
                
                // Apply the new theme
                val themeManager = ThemeManager.getInstance(requireContext())
                themeManager.setTheme(themeValue)
                themeManager.applyTheme()

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
} 
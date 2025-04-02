package com.lmizuno.smallnotesmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lmizuno.smallnotesmanager.databinding.ActivityMainBinding
import com.lmizuno.smallnotesmanager.utils.ThemeManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        ThemeManager.getInstance(this).applyTheme()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
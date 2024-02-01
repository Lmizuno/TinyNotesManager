package com.lmizuno.smallnotesmanager.Scripts

import android.content.Intent
import android.os.Build
import android.os.Bundle
import java.io.Serializable

class DeprecationManager {
    fun <T : Serializable?> getSerializable(intent: Intent, key: String, m_class: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra(key, m_class)!!
        else
            intent.getSerializableExtra(key) as T
    }

    fun <T : Serializable?> getSerializable(bundle: Bundle, key: String, m_class: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            bundle.getSerializable(key, m_class)!!
        else
            bundle.getSerializable(key) as T
    }
}
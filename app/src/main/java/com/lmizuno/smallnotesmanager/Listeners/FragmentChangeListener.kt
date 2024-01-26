package com.lmizuno.smallnotesmanager.Listeners

import androidx.fragment.app.Fragment
import java.util.Objects

interface FragmentChangeListener {
    fun replaceFragment(fragment: Fragment)
}
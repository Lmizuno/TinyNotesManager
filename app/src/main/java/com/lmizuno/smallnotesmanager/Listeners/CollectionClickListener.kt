package com.lmizuno.smallnotesmanager.Listeners

import android.os.Bundle
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Ui.collection.CollectionViewFragment
import com.lmizuno.smallnotesmanager.Ui.home.HomeFragment
import com.lmizuno.smallnotesmanager.R
interface CollectionClickListener {
    fun onClick(collection: Collection)

    fun onLongClick(collection: Collection, cardView: CardView)
}

class CollectionsClickListener(private val currentFragment: Fragment) : CollectionClickListener {
    override fun onClick(collection: Collection) {
        val fragment = CollectionViewFragment()
        val args = Bundle()
        args.putSerializable("collection", collection)
        fragment.arguments = args

        (currentFragment as HomeFragment).activity?.supportFragmentManager?.commit {
            replace(R.id.nav_host_fragment_activity_main, fragment)
            setReorderingAllowed(true)
            addToBackStack(fragment.toString())
        }
    }

    override fun onLongClick(collection: Collection, cardView: CardView) {
        //TODO
    }
}


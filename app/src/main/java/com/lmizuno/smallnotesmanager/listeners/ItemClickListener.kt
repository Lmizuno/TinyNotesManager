package com.lmizuno.smallnotesmanager.listeners

import android.content.Intent
import androidx.fragment.app.Fragment
import com.lmizuno.smallnotesmanager.CollectionPresentationActivity
import com.lmizuno.smallnotesmanager.EditorItemActivity
import com.lmizuno.smallnotesmanager.models.Item
import com.lmizuno.smallnotesmanager.ui.collection.CollectionViewFragment

interface ItemClickListener {
    fun onClick(item: Item)
}

class ItemsClickListener(private val currentFragment: Fragment) : ItemClickListener {
    override fun onClick(item: Item) {
        val colViewFrag = (currentFragment as? CollectionViewFragment)

        if (colViewFrag != null) {
            if (colViewFrag.editorToggle) {
                //Edit mode
                val intent =
                    Intent(currentFragment.requireContext(), EditorItemActivity::class.java)
                intent.putExtra("item", item)
                intent.putExtra("intent", "update")

                colViewFrag.editorItemActivityResultLauncher.launch(intent)
            } else {
                //Presentation mode
                val intent = Intent(
                    currentFragment.requireContext(), CollectionPresentationActivity::class.java
                )
                intent.putExtra("collection", colViewFrag.currentCollection)
                intent.putExtra("item", item)
                colViewFrag.startActivity(intent)
            }
        }
    }
}
package com.lmizuno.smallnotesmanager.listeners

import androidx.cardview.widget.CardView
import com.lmizuno.smallnotesmanager.Models.Collection

interface CollectionClickListener {
    fun onClick(collection: Collection)

    fun onLongClick(collection: Collection, cardView: CardView)
}
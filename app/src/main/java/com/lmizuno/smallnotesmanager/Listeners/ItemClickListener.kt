package com.lmizuno.smallnotesmanager.Listeners

import androidx.cardview.widget.CardView
import com.lmizuno.smallnotesmanager.Models.Item

interface ItemClickListener {
    fun onClick(item: Item)

    fun onLongClick(item: Item, cardView: CardView)
}
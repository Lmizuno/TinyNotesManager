package com.lmizuno.smallnotesmanager.Listeners

import com.lmizuno.smallnotesmanager.Models.Item

interface ItemClickListener {
    fun onClick(item: Item)
}

class ItemsClickListener() : ItemClickListener {
    override fun onClick(item: Item) {
        TODO("Not yet implemented")
    }
}
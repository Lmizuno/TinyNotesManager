package com.lmizuno.smallnotesmanager.Adapters

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.lmizuno.smallnotesmanager.Models.Item
import com.lmizuno.smallnotesmanager.Listeners.ItemClickListener

class ItemListAdapter(
    private val itemList: List<Item>,
    private val listener: ItemClickListener
) :
    RecyclerView.Adapter<ItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.collection_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.nameTextView.text = currentItem.title

        holder.itemContainer.setOnClickListener{
            listener.onClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var itemContainer: CardView = itemView.findViewById(R.id.collection_container)
    var nameTextView: TextView = itemView.findViewById(R.id.collection_name)
}
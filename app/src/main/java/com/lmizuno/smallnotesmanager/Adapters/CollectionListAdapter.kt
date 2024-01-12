package com.lmizuno.smallnotesmanager.Adapters

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.listeners.CollectionClickListener

class CollectionListAdapter(
    private val itemList: List<Collection>,
    private val listener: CollectionClickListener
) :
    RecyclerView.Adapter<CollectionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.collection_list, parent, false)
        return CollectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.nameTextView.text = currentItem.name

        //Add as much logic as needed here
        holder.collectionContainer.setOnClickListener{
            listener.onClick(currentItem)
        }

        holder.collectionContainer.setOnLongClickListener{
            //This will trigger organizing order event (drag and drop)
            listener.onLongClick(currentItem, holder.collectionContainer)
            return@setOnLongClickListener false
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}

class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var collectionContainer: CardView = itemView.findViewById(R.id.collection_container)
    var nameTextView: TextView = itemView.findViewById(R.id.collection_name)
}

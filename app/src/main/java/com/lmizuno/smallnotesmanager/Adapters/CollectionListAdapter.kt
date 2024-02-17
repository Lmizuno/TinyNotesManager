package com.lmizuno.smallnotesmanager.Adapters

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Listeners.CollectionClickListener

class CollectionListAdapter(
    private val collectionList: List<Collection>,
    private val listener: CollectionClickListener
) :
    RecyclerView.Adapter<CollectionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.collection_list, parent, false)
        return CollectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val currentItem = collectionList[position]

        holder.nameTextView.text = currentItem.name
        holder.decriptionTextView.text = currentItem.description

        holder.collectionContainer.setOnClickListener{
            listener.onClick(currentItem)
        }

        holder.collectionContainer.setOnLongClickListener{
            listener.onLongClick(currentItem, holder.collectionContainer)
            return@setOnLongClickListener false
        }
    }

    override fun getItemCount(): Int {
        return collectionList.size
    }
}

class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var collectionContainer: CardView = itemView.findViewById(R.id.collection_container)
    var nameTextView: TextView = itemView.findViewById(R.id.collection_name)
    var decriptionTextView: TextView = itemView.findViewById(R.id.collection_description)
}

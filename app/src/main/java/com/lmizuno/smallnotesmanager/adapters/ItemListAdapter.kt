package com.lmizuno.smallnotesmanager.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.R
import com.lmizuno.smallnotesmanager.listeners.ItemClickListener
import com.lmizuno.smallnotesmanager.models.Item
import java.time.LocalTime
import java.util.Collections

class ItemListAdapter(
    var itemList: ArrayList<Item>, private val listener: ItemClickListener
) : RecyclerView.Adapter<ItemViewHolder>(), ItemMoveCallback.ItemTouchHelperContract {

    var timeLastUpdated: LocalTime = LocalTime.now() // must only be modified when updated
    var timeOfLastModification: LocalTime = LocalTime.now()
    private var editorToggle: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.nameTextView.text = currentItem.title

        holder.itemContainer.setOnClickListener {
            listener.onClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getEditorToggle(): Boolean {
        return editorToggle
    }

    fun setEditorToggle(bool: Boolean) {
        this.editorToggle = bool
    }


    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(itemList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(itemList, i, i - 1)
            }
        }

        timeOfLastModification = LocalTime.now()
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: ItemViewHolder?) {
        myViewHolder?.itemContainer?.setBackgroundColor(Color.GRAY)
    }

    override fun onRowClear(myViewHolder: ItemViewHolder?) {
        myViewHolder?.itemContainer?.setBackgroundColor(Color.WHITE)
    }
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var itemContainer: CardView = itemView.findViewById(R.id.item_container)
    var nameTextView: TextView = itemView.findViewById(R.id.item_title)
}
package com.lmizuno.smallnotesmanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.R
import com.lmizuno.smallnotesmanager.models.Item
import io.noties.markwon.Markwon

class PresentationAdapter(private var items: List<Item>, private val markwon: Markwon) :
    RecyclerView.Adapter<PresentationAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.pageTextView)
        val title: TextView = itemView.findViewById(R.id.pageTitle)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): PresentationAdapter.Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.page_presentation_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PresentationAdapter.Pager2ViewHolder, position: Int) {
        items[position].content.let { markwon.setMarkdown(holder.content, it) }
        holder.title.text = items[position].title
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
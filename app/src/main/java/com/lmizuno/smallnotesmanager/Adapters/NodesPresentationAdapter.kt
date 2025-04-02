package com.lmizuno.smallnotesmanager.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.Models.Note
import com.lmizuno.smallnotesmanager.R
import io.noties.markwon.Markwon

class NodesPresentationAdapter(private var notes: List<Note>, private val markwon: Markwon) :
    RecyclerView.Adapter<NodesPresentationAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.pageTextView)
        val title: TextView = itemView.findViewById(R.id.pageTitle)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NodesPresentationAdapter.Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.page_presentation_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NodesPresentationAdapter.Pager2ViewHolder, position: Int) {
        notes[position].content.let { markwon.setMarkdown(holder.content, it) }
        holder.title.text = notes[position].name
    }

    override fun getItemCount(): Int {
        return notes.size
    }
}
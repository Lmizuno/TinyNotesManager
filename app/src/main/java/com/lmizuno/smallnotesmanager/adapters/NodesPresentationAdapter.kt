package com.lmizuno.smallnotesmanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.R
import com.lmizuno.smallnotesmanager.models.Node
import com.lmizuno.smallnotesmanager.models.NodeType
import com.lmizuno.smallnotesmanager.models.Note
import io.noties.markwon.Markwon

class NodesPresentationAdapter(private var nodes: List<Node>, private val markwon: Markwon) :
    RecyclerView.Adapter<NodesPresentationAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.pageTextView)
        val title: TextView = itemView.findViewById(R.id.pageTitle)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): NodesPresentationAdapter.Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.page_presentation_view, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: NodesPresentationAdapter.Pager2ViewHolder,
        position: Int
    ) {
        holder.title.text = nodes[position].name
        when {
            nodes[position].type == NodeType.FOLDER -> {
                //val folder = nodes[position] as Folder
            }
            nodes[position].type == NodeType.NOTE -> {
                val note = nodes[position] as Note
                note.content.let { markwon.setMarkdown(holder.content, it) }
            }
        }
    }

    override fun getItemCount(): Int {
        return nodes.size
    }
}
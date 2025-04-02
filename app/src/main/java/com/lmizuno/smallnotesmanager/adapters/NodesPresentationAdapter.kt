package com.lmizuno.smallnotesmanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
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
        val scrollView: View = itemView.findViewById(R.id.pageScrollview)
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
                // Hide content for folders and center the title
                holder.scrollView.visibility = View.GONE
                holder.title.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
            }
            nodes[position].type == NodeType.NOTE -> {
                // Show content for notes
                holder.scrollView.visibility = View.VISIBLE
                holder.title.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                }
                //holder.title.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                val note = nodes[position] as Note
                note.content.let { markwon.setMarkdown(holder.content, it) }
            }
        }
    }

    override fun getItemCount(): Int {
        return nodes.size
    }
}
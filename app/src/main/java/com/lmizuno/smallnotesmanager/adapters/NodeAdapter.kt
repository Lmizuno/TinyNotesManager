package com.lmizuno.smallnotesmanager.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.R
import com.lmizuno.smallnotesmanager.databinding.ItemNodeBinding
import com.lmizuno.smallnotesmanager.models.Folder
import com.lmizuno.smallnotesmanager.models.Node

class NodeAdapter(
    private var nodes: List<Node>, private val onNodeClick: (Node) -> Unit
) : RecyclerView.Adapter<NodeAdapter.NodeViewHolder>() {

    class NodeViewHolder(
        private val binding: ItemNodeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(node: Node, onNodeClick: (Node) -> Unit) {
            when (node) {
                is Folder -> {
                    binding.textName.text = node.name
                    binding.textDescription.text = node.description
                }

                else -> binding.textName.text = node.name
            }

            // Set icon and color
            binding.iconType.apply {
                setImageResource(
                    when (node) {
                        is Folder -> R.drawable.baseline_folder_24
                        else -> R.drawable.baseline_edit_square_24
                    }
                )
                // Set icon color to secondary
                setColorFilter(
                    context.getColor(R.color.primary), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            binding.root.setOnClickListener { onNodeClick(node) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
        val binding = ItemNodeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
        holder.bind(nodes[position], onNodeClick)
    }

    override fun getItemCount() = nodes.size

    fun updateNodes(newNodes: List<Node>) {
        nodes = newNodes
        notifyDataSetChanged()
    }
} 
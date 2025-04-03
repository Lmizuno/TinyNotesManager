package com.lmizuno.smallnotesmanager.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.R
import com.lmizuno.smallnotesmanager.databinding.ItemNodeBinding
import com.lmizuno.smallnotesmanager.models.Folder
import com.lmizuno.smallnotesmanager.models.Node
import com.lmizuno.smallnotesmanager.viewmodels.NodeViewModel
import java.time.LocalTime
import java.util.Collections

class NodeAdapter(
    private var nodes: MutableList<Node>, 
    private val onNodeClick: (Node) -> Unit
) : RecyclerView.Adapter<NodeAdapter.NodeViewHolder>(), NodeMoveCallback.NodeTouchHelperContract {

    private var timeLastUpdated: LocalTime = LocalTime.now() // must only be modified when updated
    private var timeOfLastModification: LocalTime = LocalTime.now()
    private var viewModel: NodeViewModel? = null
    private var pendingOrderUpdate = false

    class NodeViewHolder(
        val binding: ItemNodeBinding
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
        nodes = newNodes.toMutableList()
        notifyDataSetChanged()
        timeLastUpdated = LocalTime.now()
    }

    // Set the ViewModel reference
    fun setViewModel(viewModel: NodeViewModel) {
        this.viewModel = viewModel
    }

    // Reordering functionality
    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(nodes, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(nodes, i, i - 1)
            }
        }

        // Update order values for all affected nodes
        for (i in nodes.indices) {
            nodes[i].order = i.toLong()
        }

        timeOfLastModification = LocalTime.now()
        pendingOrderUpdate = true
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: NodeViewHolder?) {
        myViewHolder?.binding?.root?.setBackgroundColor(Color.LTGRAY)
    }

    override fun onRowClear(myViewHolder: NodeViewHolder?) {
        myViewHolder?.binding?.root?.let { view ->
            view.background = null
        }
    }

    // Add method to commit order changes
    fun commitOrderChanges() {
        if (pendingOrderUpdate && viewModel != null) {
            viewModel?.updateNodesOrder(nodes) { success ->
                if (success) {
                    pendingOrderUpdate = false
                }
            }
        }
    }

    fun isPendingOrderUpdate(): Boolean {
        return pendingOrderUpdate
    }
} 
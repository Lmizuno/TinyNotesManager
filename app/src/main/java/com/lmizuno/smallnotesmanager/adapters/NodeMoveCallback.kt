package com.lmizuno.smallnotesmanager.adapters

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class NodeMoveCallback(private val mAdapter: NodeTouchHelperContract) : ItemTouchHelper.Callback() {
    override fun isLongPressDragEnabled(): Boolean {
        // Always enable long press dragging
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {}
    
    override fun getMovementFlags(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        mAdapter.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?, actionState: Int
    ) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is NodeAdapter.NodeViewHolder) {
                val myViewHolder: NodeAdapter.NodeViewHolder = viewHolder
                mAdapter.onRowSelected(myViewHolder)
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is NodeAdapter.NodeViewHolder) {
            val myViewHolder: NodeAdapter.NodeViewHolder = viewHolder
            mAdapter.onRowClear(myViewHolder)
            
            // Save order changes when dragging ends
            if (mAdapter is NodeAdapter && (mAdapter as NodeAdapter).isPendingOrderUpdate()) {
                (mAdapter as NodeAdapter).commitOrderChanges()
            }
        }
    }

    interface NodeTouchHelperContract {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(myViewHolder: NodeAdapter.NodeViewHolder?)
        fun onRowClear(myViewHolder: NodeAdapter.NodeViewHolder?)
    }
} 
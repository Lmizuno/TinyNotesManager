package com.lmizuno.smallnotesmanager.Adapters

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.Callback
import androidx.recyclerview.widget.RecyclerView

class ItemMoveCallback(private val mAdapter: ItemTouchHelperContract) : Callback() {
    override fun isLongPressDragEnabled(): Boolean {
        return mAdapter.getEditorToggle()
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {}
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        mAdapter.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?,
        actionState: Int
    ) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is ItemViewHolder) {
                val myViewHolder: ItemViewHolder = viewHolder
                mAdapter.onRowSelected(myViewHolder)
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is ItemViewHolder) {
            val myViewHolder: ItemViewHolder = viewHolder
            mAdapter.onRowClear(myViewHolder)
        }
    }

    interface ItemTouchHelperContract {
        fun getEditorToggle(): Boolean
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(myViewHolder: ItemViewHolder?)
        fun onRowClear(myViewHolder: ItemViewHolder?)
    }
}
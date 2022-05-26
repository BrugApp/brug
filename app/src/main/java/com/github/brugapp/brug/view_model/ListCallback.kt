package com.github.brugapp.brug.view_model

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.ui.CallbackUI

class ListCallback<T>(
    private val onSwipeLayoutPair: Pair<Drawable, Int>,
    private val listAdapterPair: Pair<MutableList<T>, RecyclerView.Adapter<ListViewHolder>>,
    private val onSwipeActions: (T, Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    /* SWIPE TO DELETE (LEFT AND RIGHT) */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val list = listAdapterPair.first
        val eltToDelete = list[position]
        onSwipeActions(eltToDelete, position)
    }

    // Drawing the red "delete" background when swiping to delete
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val callbackUI = CallbackUI(c, viewHolder, dX)
        callbackUI.setSwipeUI(onSwipeLayoutPair)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
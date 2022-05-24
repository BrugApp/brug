package com.github.brugapp.brug.view_model

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.ui.CallbackUI

private const val MOVED_TEXT: String = "Item has been moved."
private const val UNDO_TEXT: String = "Undo"

class ListCallback<T>(
    private val swipeValue: Int,
    private val onSwipeLayoutPair: Pair<Drawable, Int>,
    private val listAdapterPair: Pair<MutableList<T>, RecyclerView.Adapter<ListViewHolder>>,
    private val onSwipeActions: (T, Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, swipeValue) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    /* SWIPE TO DELETE (LEFT AND RIGHT) */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (swipeValue != 0) {
            val position = viewHolder.adapterPosition
            val list = listAdapterPair.first
            val eltToDelete = list[position]
            onSwipeActions(eltToDelete, position)
        }
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
package com.github.brugapp.brug.view_model

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.ui.CallbackUI
import com.google.android.material.snackbar.Snackbar
import java.util.*

private const val MOVED_TEXT: String = "Item has been moved."
private const val UNDO_TEXT: String = "Undo"

class ListCallback<T>(
    private val snackBarText: String,
    private val dragSwipePair: Pair<Int, Int>,
    private val swipePair: Pair<Drawable, Int>,
    private val listAdapterPair: Pair<MutableList<T>, RecyclerView.Adapter<ListViewHolder>>,
    private val onSwipeActions: (T) -> Unit
    ): ItemTouchHelper.SimpleCallback(dragSwipePair.first, dragSwipePair.second)
{
    /* DRAG TO REORDER (UP AND DOWN THE LIST) */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return if(dragSwipePair.first != 0){
            val startPosition = viewHolder.adapterPosition
            val endPosition = target.adapterPosition

            val list = listAdapterPair.first
            Collections.swap(list, startPosition, endPosition)
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)

            Snackbar.make(recyclerView,
                MOVED_TEXT,
                Snackbar.LENGTH_LONG)
                .show()
            true
        } else false
    }

    /* SWIPE TO DELETE (LEFT AND RIGHT) */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if(dragSwipePair.second != 0){
            val position = viewHolder.adapterPosition
            val list = listAdapterPair.first
            val eltToDelete = list[position]
            onSwipeActions(eltToDelete)
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
        callbackUI.setSwipeUI(swipePair)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
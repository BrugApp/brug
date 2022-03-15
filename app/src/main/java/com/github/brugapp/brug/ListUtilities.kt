package com.github.brugapp.brug

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.collections.ArrayList

class ListUtilities<T>(
    context: Context,
    listView: RecyclerView,
    listViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
    itemsList: ArrayList<T>) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24) !! // To trigger NullPointerException if icon not found
    private val swipeBG: ColorDrawable = ColorDrawable(Color.parseColor("#d65819")) // To move in a color database

    // To implement swipe to delete animation + move to reorder
    val listCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
        ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
        /* DRAG TO REORDER (UP AND DOWN THE LIST) */
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            /* MISSING TEST CASE, HENCE COMMENTED FOR NOW */
            val startPosition = viewHolder.adapterPosition
            val endPosition = target.adapterPosition

//            val movedElt = itemsList[startPosition]

            Collections.swap(itemsList, startPosition, endPosition)
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)

            Snackbar.make(listView,
                "Item has been moved from $startPosition to $endPosition",
                Snackbar.LENGTH_LONG)
                .show()
            return true
        }

        /* HERE IS WHERE TO CALL THE ACTUAL DELETE FUNCTION */
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val deletedElt = itemsList.removeAt(position)
            listViewAdapter.notifyItemRemoved(position)

            Snackbar.make(listView, "TEST OF REMOVAL", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    itemsList.add(position, deletedElt)
                    listViewAdapter.notifyItemInserted(position)
                }.show()
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
            val chatView = viewHolder.itemView
            val iconMargin = (chatView.height - deleteIcon.intrinsicHeight) / 2

            deleteIcon.setTint(Color.WHITE)

            swipeBG.bounds = computeBGBounds(dX, chatView)
            deleteIcon.bounds = computeIconBounds(dX, iconMargin, chatView)

            swipeBG.draw(c)
            c.save()
            c.clipRect(swipeBG.bounds)
            deleteIcon.draw(c)
            c.restore()

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }


        private fun computeBGBounds(dX: Float, chatView: View): Rect {
            val leftOffset: Int
            val rightOffset: Int
            if(dX > 0){
                leftOffset = chatView.left
                rightOffset = dX.toInt()
            } else {
                leftOffset = chatView.right + dX.toInt()
                rightOffset = chatView.right
            }

            return Rect(leftOffset, chatView.top, rightOffset, chatView.bottom)
        }


        private fun computeIconBounds(dX: Float, iconMargin: Int, chatView: View): Rect {
            val leftOffset: Int
            val rightOffset: Int
            if(dX > 0){
                leftOffset = chatView.left + iconMargin
                rightOffset = chatView.left + iconMargin + deleteIcon.intrinsicWidth
            } else {
                leftOffset = chatView.right - iconMargin - deleteIcon.intrinsicWidth
                rightOffset = chatView.right - iconMargin
            }

            return Rect(leftOffset, chatView.top + iconMargin, rightOffset, chatView.bottom - iconMargin)
        }
    }

}
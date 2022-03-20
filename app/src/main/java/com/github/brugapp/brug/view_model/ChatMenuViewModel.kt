package com.github.brugapp.brug.view_model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.ItemsListAdapter
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Item
import com.google.android.material.snackbar.Snackbar
import java.util.*

class ItemsMenuViewModel : ViewModel() {
    private val myItemsList: MutableList<Item> by lazy {
        loadItems()
    }

    private fun loadItems(): MutableList<Item> {
        // TODO in the future: Refactor to fetch values from actual database
        return mutableListOf(
            Item("Phone", R.drawable.ic_baseline_smartphone_24, "Samsung Galaxy S22"),
            Item("Wallet", R.drawable.ic_baseline_account_balance_wallet_24, "With all my belongings"),
            Item("BMW Key", R.drawable.ic_baseline_car_rental_24, "BMW M3 F80 Competition"),
            Item("Keys", R.drawable.ic_baseline_vpn_key_24,"House and everything else")
        )
    }

    fun getItemsList(): MutableList<Item> {
        return myItemsList
    }

    inner class ItemsListCallback(val context: Context, private val listViewAdapter: ItemsListAdapter, private val onDeleteStr: String)
        : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
        ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
    ) {
        private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24) !! // To trigger NullPointerException if icon not found
        private val swipeBG: ColorDrawable = ColorDrawable(Color.parseColor("#d65819")) // To move in a color database (maybe ?)

        /* DRAG TO REORDER (UP AND DOWN THE LIST) */
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPosition = viewHolder.adapterPosition
            val endPosition = target.adapterPosition

            val list = listViewAdapter.getItemsList()
            Collections.swap(list, startPosition, endPosition)
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)

            Snackbar.make(recyclerView,
                "Item has been moved from $startPosition to $endPosition",
                Snackbar.LENGTH_LONG)
                .show()
            return true
        }

        /* SWIPE TO DELETE (LEFT AND RIGHT) */
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val list = listViewAdapter.getItemsList()
            val deletedElt = list.removeAt(position)
            listViewAdapter.notifyItemRemoved(position)

            Snackbar.make(viewHolder.itemView, onDeleteStr, Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    list.add(position, deletedElt)
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
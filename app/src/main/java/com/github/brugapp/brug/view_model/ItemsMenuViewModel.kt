package com.github.brugapp.brug.view_model

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.ui.CallbackUI
import com.google.android.material.snackbar.Snackbar
import java.util.*

private const val DELETE_TEXT: String = "Item has been deleted."
private const val MOVED_TEXT: String = "Item has been moved."
private const val UNDO_TEXT: String = "Undo"

/**
 * ViewModel of the Items Menu UI, handling its UI logic.
 */
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

    /**
     * Getter for the list of items.
     */
    fun getItemsList(): MutableList<Item> {
        return myItemsList
    }

    /**
     * Callback for events on the list of items.
     */
    inner class ItemsListCallback(
        private val leftSwipePair: Pair<Drawable, Int>,
        private val rightSwipePair: Pair<Drawable, Int>,
        private val listViewAdapter: ItemsListAdapter
    ) : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
        ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
    ) {

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
                MOVED_TEXT,
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

            Snackbar.make(viewHolder.itemView, DELETE_TEXT, Snackbar.LENGTH_LONG)
                .setAction(UNDO_TEXT) {
                    list.add(position, deletedElt)
                    listViewAdapter.notifyItemInserted(position)
                }.show()

        }

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
            callbackUI.setSwipeUI(leftSwipePair, rightSwipePair)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}
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
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.User
import com.google.android.material.snackbar.Snackbar

class ChatMenuViewModel : ViewModel() {
    private val myChatList: MutableList<Conversation> by lazy {
        loadConversations()
    }

    private fun loadConversations(): MutableList<Conversation> {
        // TODO in the future: Refactor to fetch values from actual database
        return mutableListOf(
            Conversation(
                User("Anna", "Rosenberg", "anna@rosenberg.com", "123456"),
                listOf("Me: Where are you located ? I'm near the center of Lausanne, so feel free to propose me any location in Lausanne")),
            Conversation(
                User("Henry", "Crawford", "crawform@services.co.uk", "129271"),
                listOf("Hey ! I might have found your wallet yesterday near the EPFL campus")),
            Conversation(
                User("Jenna", "Hewitt", "jenna.hewitt@epfl.ch", "310827"),
                listOf("Me: Fine, lets meet on Saturday then !")),
            Conversation(
                User("John", "Newmann", "john@microsoft.com", "1892122"),
                listOf("Give me my money back you thief !!!"))
        )
    }

    fun getChatList(): MutableList<Conversation> {
        return myChatList
    }

    inner class ChatListCallback(val context: Context, private val listViewAdapter: ChatListAdapter)
        : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
    ) {
        private val checkText = "The conversation has been marked as resolved."
        private val checkIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_check_circle_outline_24) !! // To trigger NullPointerException if icon not found
        private val swipeBG: ColorDrawable = ColorDrawable(Color.parseColor("#15b800")) // To move in a color database (maybe ?)

        /* DRAG TO REORDER (UP AND DOWN THE LIST) */
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        /* SWIPE TO DELETE (LEFT AND RIGHT) */
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val list = listViewAdapter.getChatList()
            val deletedElt = list.removeAt(position)
            listViewAdapter.notifyItemRemoved(position)

            Snackbar.make(viewHolder.itemView, checkText, Snackbar.LENGTH_LONG)
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
            val iconMargin = (chatView.height - checkIcon.intrinsicHeight) / 2

            checkIcon.setTint(Color.WHITE)

            swipeBG.bounds = computeBGBounds(dX, chatView)
            checkIcon.bounds = computeIconBounds(dX, iconMargin, chatView)

            swipeBG.draw(c)
            c.save()
            c.clipRect(swipeBG.bounds)
            checkIcon.draw(c)
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
                rightOffset = chatView.left + iconMargin + checkIcon.intrinsicWidth
            } else {
                leftOffset = chatView.right - iconMargin - checkIcon.intrinsicWidth
                rightOffset = chatView.right - iconMargin
            }

            return Rect(leftOffset, chatView.top + iconMargin, rightOffset, chatView.bottom - iconMargin)
        }
    }
}
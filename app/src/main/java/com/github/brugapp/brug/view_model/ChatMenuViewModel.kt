package com.github.brugapp.brug.view_model

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.model.ChatMessage
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.ui.CallbackUI
import com.google.android.material.snackbar.Snackbar

private const val CHECK_TEXT: String = "The conversation has been marked as resolved."
private const val UNDO_TEXT: String = "Undo"

/**
 * ViewModel of the Chat Menu UI, handling its UI logic.
 */
class ChatMenuViewModel : ViewModel() {
    private val myChatList: MutableList<Conversation> by lazy {
        loadConversations()
    }

    private fun loadConversations(): MutableList<Conversation> {
        // TODO in the future: Refactor to fetch values from actual database
        return mutableListOf(
            Conversation(
                User("Anna", "Rosenberg", "anna@rosenberg.com", "123456"),
                listOf(ChatMessage(
                        "Me: Where are you located ? I'm near the center of Lausanne, so feel free to propose me any location in Lausanne",
                        "2022.03.19",
                        "Me"))),
            Conversation(
                User("Henry", "Crawford", "crawform@services.co.uk", "129271"),
                listOf(ChatMessage(
                    "Hey ! I might have found your wallet yesterday near the EPFL campus",
                    "2022.03.19",
                    "Henry"))),
            Conversation(
                User("Jenna", "Hewitt", "jenna.hewitt@epfl.ch", "310827"),
                listOf(ChatMessage(
                    "Fine, lets meet on Saturday then !",
                    "2022.03.19",
                    "Me"))),
            Conversation(
                User("John", "Newmann", "john@microsoft.com", "1892122"),
                listOf(ChatMessage(
                    "Give me my money back you thief !!!",
                    "2022.03.19",
                    "John")))
        )
    }

    /**
     * Getter for the list of conversations.
     */
    fun getChatList(): MutableList<Conversation> {
        return myChatList
    }

    /**
     * Callback for events on the list of conversations.
     */
    inner class ChatListCallback(
        private val leftSwipePair: Pair<Drawable, Int>,
        private val rightSwipePair: Pair<Drawable, Int>,
        private val listViewAdapter: ChatListAdapter)
        : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
    ) {

        /* DRAG TO REORDER (UP AND DOWN THE LIST) */
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
            val list = listViewAdapter.getChatList()
            val deletedElt = list.removeAt(position)
            listViewAdapter.notifyItemRemoved(position)

            Snackbar.make(viewHolder.itemView, CHECK_TEXT, Snackbar.LENGTH_LONG)
                .setAction(UNDO_TEXT) {
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
            val callbackUI = CallbackUI(c, viewHolder, dX)
            callbackUI.setSwipeUI(leftSwipePair, rightSwipePair)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}
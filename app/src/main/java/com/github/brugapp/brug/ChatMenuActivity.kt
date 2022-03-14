package com.github.brugapp.brug

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar

private const val SEARCH_HINT = "Search for a conversation…"

class ChatMenuActivity : AppCompatActivity() {
    private val conversations = ArrayList<ChatViewModel>()
    private val listViewAdapter = ChatCustomAdapter(conversations)
    private lateinit var listView: RecyclerView
    private lateinit var deleteIcon: Drawable

    private var swipeBG: ColorDrawable = ColorDrawable(Color.parseColor("#d65819")) // To move in a color database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_menu)

        initChatList()
        initNavigationBar()
    }

    // Initializing the top-bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_top_bar, menu)

        val searchChat = menu?.findItem(R.id.search_box)
        val searchView = searchChat?.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = SEARCH_HINT
        return super.onCreateOptionsMenu(menu)
    }


    private fun initChatList() {
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete_24) !! // To trigger NullPointerException if icon not found
        listView = findViewById(R.id.chat_listview)
        listView.layoutManager = LinearLayoutManager(this)

        conversations.add(ChatViewModel(R.mipmap.ic_launcher, "Henry", "Hey ! I might have found your wallet yesterday near the EPFL campus"))
        conversations.add(ChatViewModel(R.mipmap.ic_launcher, "Jenna", "Me: Fine, lets meet on Saturday then !"))
        conversations.add(ChatViewModel(R.mipmap.ic_launcher, "John", "Give me my money back you thief !!!"))
        conversations.add(ChatViewModel(R.mipmap.ic_launcher, "Anna", "Me: Where are you located ? I'm near the center of Lausanne, so feel free to propose me any location in Lausanne"))

        ItemTouchHelper(listCallback).attachToRecyclerView(listView)
        listView.adapter = listViewAdapter
    }

    private fun initNavigationBar(){
        val bottomNavBar = findViewById<NavigationBarView>(R.id.bottom_navigation)
        bottomNavBar.setOnItemSelectedListener {menuItem ->
            when(menuItem.itemId){
                R.id.items_list_menu_button -> {
                    startActivity(Intent(this, ItemsMenuActivity::class.java))
                    true
                }
                R.id.qr_scan_menu_button -> {
                    startActivity(Intent(this, QrCodeScannerActivity::class.java))
                    true
                }
                R.id.chat_menu_button -> {
                    true
                }
                else -> false
            }
        }
        bottomNavBar.selectedItemId = R.id.chat_menu_button
    }


    // To implement swipe to delete animation + move to reorder
    private val listCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
        /* DRAG TO REORDER (UP AND DOWN THE LIST) */
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            /* MISSING TEST CASE, HENCE COMMENTED FOR NOW */
//            val startPosition = viewHolder.adapterPosition
//            val endPosition = target.adapterPosition
//
//            val movedElt = data[startPosition]
//
//            Collections.swap(data, startPosition, endPosition)
//            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
//
//            Snackbar.make(listView,
//                "Item \"${movedElt.title}\" has been moved from ${startPosition} to ${endPosition}",
//                Snackbar.LENGTH_LONG)
//                .show()
            return true
        }

        /* HERE IS WHERE TO CALL THE ACTUAL DELETE FUNCTION */
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val deletedElt = conversations.removeAt(position)
            listViewAdapter.notifyItemRemoved(position)

            Snackbar.make(listView, "Conversation with \"${deletedElt.name}\" is deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    conversations.add(position, deletedElt)
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
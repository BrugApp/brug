package com.github.brugapp.brug

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.collections.ArrayList

class ItemsMenuActivity : AppCompatActivity() {
    private val data = ArrayList<ItemsViewModel>()
    private val listViewAdapter = ItemsCustomAdapter(data)
    private lateinit var listView: RecyclerView
    private lateinit var deleteIcon: Drawable

    private var swipeBG: ColorDrawable = ColorDrawable(Color.parseColor("#d65819")) // To move in a color database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_menu)

        initItemsList()
        initFloatingAddButton()
        initNavigationBar()
    }

    // For the searchbar when pressing on the top bar's search icon
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_top_bar, menu)

        val item = menu?.findItem(R.id.search_items)
        val searchView = item?.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = "Search items here..."
        return super.onCreateOptionsMenu(menu)
    }

    // For the settings icon on top bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if(id == R.id.my_settings){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initItemsList(){
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete_24) !! // To trigger NullPointerException if icon not found
        listView = findViewById(R.id.items_listview)
        listView.layoutManager = LinearLayoutManager(this)

        data.add(ItemsViewModel(R.drawable.ic_baseline_smartphone_24, "Phone", "Samsung Galaxy S22"))
        data.add(ItemsViewModel(R.drawable.ic_baseline_account_balance_wallet_24, "Wallet", "With all my belongings"))
        data.add(ItemsViewModel(R.drawable.ic_baseline_car_rental_24, "BMW Key", "BMW M3 F80 Competition"))
        data.add(ItemsViewModel(R.drawable.ic_baseline_vpn_key_24, "Keys", "House and everything else"))

        ItemTouchHelper(listCallback).attachToRecyclerView(listView)
        listView.adapter = listViewAdapter
    }

    private fun initFloatingAddButton(){
        val addButton = findViewById<FloatingActionButton>(R.id.add_new_item_button)
        addButton.setOnClickListener{ view ->
            Snackbar.make(view, "Actual behavior coming soon...", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }
    }

    private fun initNavigationBar(){
        NavigationBarView.OnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.main_menu -> {
                    true
                }
                R.id.scan_page -> {
                    false
                }
                R.id.chat_page -> {
                    false
                }
                else -> false
            }
        }
    }


    // To implement swipe to delete animation + move to reorder
    private val listCallback = object : SimpleCallback(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPosition = viewHolder.adapterPosition
            val endPosition = target.adapterPosition

            Collections.swap(data, startPosition, endPosition)
            listView.adapter?.notifyItemMoved(startPosition, endPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val deletedElt = data.removeAt(position)
            listViewAdapter.notifyItemRemoved(position)

            Snackbar.make(listView, "Item \"${deletedElt.title}\" is deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    data.add(position, deletedElt)
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
            val itemView = viewHolder.itemView
            val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

            deleteIcon.setTint(Color.WHITE)
            val bgOffsets: Array<Int>
            val delIconOffsets: Array<Int>

            if(dX > 0){
                bgOffsets = arrayOf(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                delIconOffsets = arrayOf(
                    itemView.left + iconMargin,
                    itemView.top + iconMargin,
                    itemView.left + iconMargin + deleteIcon.intrinsicWidth,
                    itemView.bottom - iconMargin)
            } else {
                bgOffsets = arrayOf(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                delIconOffsets = arrayOf(
                    itemView.right - iconMargin - deleteIcon.intrinsicWidth,
                    itemView.top + iconMargin,
                    itemView.right - iconMargin,
                    itemView.bottom - iconMargin)
            }

            swipeBG.setBounds(bgOffsets[0], bgOffsets[1], bgOffsets[2], bgOffsets[3])
            deleteIcon.setBounds(delIconOffsets[0], delIconOffsets[1], delIconOffsets[2], delIconOffsets[3])

            swipeBG.draw(c)
            c.save()
            c.clipRect(bgOffsets[0], bgOffsets[1], bgOffsets[2], bgOffsets[3])
            deleteIcon.draw(c)
            c.restore()

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

}
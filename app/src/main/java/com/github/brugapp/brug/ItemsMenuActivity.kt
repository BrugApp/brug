package com.github.brugapp.brug

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

private const val DUMMY_TEXT: String = "Actual behavior coming soon..."
private const val SEARCH_HINT: String = "Search items here..."

class ItemsMenuActivity : AppCompatActivity(){
    private val data = ArrayList<ItemsViewModel>()
    private lateinit var listViewAdapter:ItemsCustomAdapter
    private lateinit var listView: RecyclerView
    private lateinit var deleteIcon: Drawable

    private var swipeBG: ColorDrawable = ColorDrawable(Color.parseColor("#d65819")) // To move in a color database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_menu)

        initItemsList()
        initFloatingAddButton()
//        initNavigationBar()
    }

    // For the searchbar when pressing on the top bar's search icon
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_top_bar, menu)

        val item = menu?.findItem(R.id.search_items)
        val searchView = item?.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = SEARCH_HINT
        return super.onCreateOptionsMenu(menu)
    }

    // For the settings icon on top bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if(id == R.id.my_settings){
            Snackbar.make(window.decorView, DUMMY_TEXT, Snackbar.LENGTH_LONG)
                .show()
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

        listViewAdapter = ItemsCustomAdapter(data)
        listView.adapter= listViewAdapter

        ItemTouchHelper(listCallback).attachToRecyclerView(listView)
    }

    private fun initFloatingAddButton(){
        val addButton = findViewById<FloatingActionButton>(R.id.add_new_item_button)
        addButton.setOnClickListener{ view ->
            Snackbar.make(view, DUMMY_TEXT, Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }
    }



    // To implement swipe to delete animation + move to reorder
    private val listCallback = object : SimpleCallback(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
        /* DRAG TO REORDER (UP AND DOWN THE LIST) */
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
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

            if(direction ==  ItemTouchHelper.RIGHT) {
                val deletedElt = data.removeAt(position)
                listViewAdapter.notifyItemRemoved(position)
                Snackbar.make(
                    listView,
                    "Item \"${deletedElt.title}\" is deleted",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Undo") {
                        data.add(position, deletedElt)
                        listViewAdapter.notifyItemInserted(position)
                    }.show()
            }else {
                val itemsViewModel = data[position]
                val intent = Intent(viewHolder.itemView.context,ItemInformationActivity::class.java)
                intent.putExtra("title",itemsViewModel.title)
                intent.putExtra("description",itemsViewModel.description)
                intent.putExtra("image",itemsViewModel.image)
                intent.putExtra("lastLocation",itemsViewModel.lastLocation)
                intent.putExtra("addedOn",itemsViewModel.addedOn)
                intent.putExtra("image",itemsViewModel.image)
                startActivity(intent)
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
            val itemView = viewHolder.itemView
            val background = ColorDrawable()

            if(dX>0) {
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

                deleteIcon.setTint(Color.WHITE)

                swipeBG.bounds = computeBGBounds(dX, itemView)
                deleteIcon.bounds = computeIconBounds(dX, iconMargin, itemView)

                swipeBG.draw(c)
                c.save()
                c.clipRect(swipeBG.bounds)
                deleteIcon.draw(c)
                c.restore()
            }else{
                val backgroundColor = Color.parseColor("#32a852")
                background.color = backgroundColor
                background.bounds =computeBGBounds(dX, itemView)
                background.draw(c)

            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }


        private fun computeBGBounds(dX: Float, itemView: View): Rect {
            val leftOffset: Int
            val rightOffset: Int
            if(dX > 0){
                leftOffset = itemView.left
                rightOffset = dX.toInt()
            } else {
                leftOffset = itemView.right + dX.toInt()
                rightOffset = itemView.right
            }

            return Rect(leftOffset, itemView.top, rightOffset, itemView.bottom)
        }


        private fun computeIconBounds(dX: Float, iconMargin: Int, itemView: View): Rect {
            val leftOffset: Int = itemView.left + iconMargin
            val rightOffset: Int = itemView.left + iconMargin + deleteIcon.intrinsicWidth

            return Rect(leftOffset, itemView.top + iconMargin, rightOffset, itemView.bottom - iconMargin)
        }
    }

}
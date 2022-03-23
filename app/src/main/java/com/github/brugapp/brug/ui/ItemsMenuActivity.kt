package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.AddItemActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.ui.components.BottomNavBar
import com.github.brugapp.brug.ui.components.CustomTopBar
import com.github.brugapp.brug.view_model.ItemsListAdapter
import com.github.brugapp.brug.view_model.ItemsMenuViewModel
import com.github.brugapp.brug.view_model.ListCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val DUMMY_TEXT: String = "Actual behavior coming soon…"
private const val SEARCH_HINT: String = "Search items here…"
private const val DELETE_TEXT: String = "Item has been deleted."

class ItemsMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_menu)

        val model: ItemsMenuViewModel by viewModels()

        initItemsList(model)
        initFloatingAddButton()
        BottomNavBar().initBottomBar(this)
    }

    // For the searchbar when pressing on the top bar's search icon
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        customTopBar.inflateTopBar(menuInflater, menu, SEARCH_HINT)
        return super.onCreateOptionsMenu(menu)
    }

    // For the settings icon on top bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        customTopBar.defineTopBarActions(window.decorView, DUMMY_TEXT, item)
        return super.onOptionsItemSelected(item)
    }

    private fun initItemsList(model: ItemsMenuViewModel){
        val listView = findViewById<RecyclerView>(R.id.items_listview)
        val itemsListAdapter = ItemsListAdapter(model.getItemsList())
        { clickedItem ->
            val intent = Intent(this, ItemInformationActivity::class.java)
            intent.putExtra("image", clickedItem.getId())
            intent.putExtra("title", clickedItem.getName())
            intent.putExtra("description", clickedItem.getDescription())
            startActivity(intent)
        }

        listView.layoutManager = LinearLayoutManager(this)

        val dragPair = Pair(
            ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
            ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
        )

        val swipePair = Pair(
            ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete_24) !!,
            ContextCompat.getColor(this, R.color.list_item_del_BG))

        val listAdapterPair = Pair(
            model.getItemsList(),
            itemsListAdapter
        )

        val listCallback = ListCallback(DELETE_TEXT, dragPair, swipePair, listAdapterPair)
        ItemTouchHelper(listCallback).attachToRecyclerView(listView)

        listView.adapter = itemsListAdapter
    }

    private fun initFloatingAddButton(){
        val addButton = findViewById<FloatingActionButton>(R.id.add_new_item_button)

        addButton.setOnClickListener{
            val myIntent = Intent(this, AddItemActivity::class.java).apply {  }
            startActivity(myIntent)
        }
    }

    companion object {
        private val customTopBar = CustomTopBar()
    }
}
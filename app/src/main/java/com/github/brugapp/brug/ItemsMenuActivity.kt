package com.github.brugapp.brug

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar

private const val DUMMY_TEXT: String = "Actual behavior coming soon…"
private const val SEARCH_HINT: String = "Search items here…"
private const val DELETE_TEXT: String = "Item has been deleted."

class ItemsMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_menu)

        initItemsList()
        initFloatingAddButton()
        initNavigationBar()
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

    private fun initItemsList(){
        val listView = findViewById<RecyclerView>(R.id.items_listview)
        val listViewAdapter = ListCustomAdapter(
            R.layout.list_item_layout,
            R.id.list_item_icon,
            R.id.list_item_title,
            R.id.list_item_desc
        ) { // HERE TO IMPLEMENT ONCLICK ACTIONS
                item ->
            //It would be better to get an item from a ListViewModel
            val intent = Intent(this,ItemInformationActivity::class.java)
            intent.putExtra("title",item.title)
            intent.putExtra("description",item.desc)
            intent.putExtra("image",item.iconId)
            //intent.putExtra("lastLocation",item.lastLocation)
            //intent.putExtra("addedOn",item.addedOn)
            startActivity(intent)

        }
        val listCallback = ListCustomCallback(this, listViewAdapter, DELETE_TEXT)
        listView.layoutManager = LinearLayoutManager(this)

        listViewAdapter.addEntry(ListViewModel(R.drawable.ic_baseline_smartphone_24, "Phone", "Samsung Galaxy S22"))
        listViewAdapter.addEntry(ListViewModel(R.drawable.ic_baseline_account_balance_wallet_24, "Wallet", "With all my belongings"))
        listViewAdapter.addEntry(ListViewModel(R.drawable.ic_baseline_car_rental_24, "BMW Key", "BMW M3 F80 Competition"))
        listViewAdapter.addEntry(ListViewModel(R.drawable.ic_baseline_vpn_key_24, "Keys", "House and everything else"))

        ItemTouchHelper(listCallback).attachToRecyclerView(listView)
        listView.adapter = listViewAdapter
    }

    private fun initFloatingAddButton(){
        val addButton = findViewById<FloatingActionButton>(R.id.add_new_item_button)
        addButton.setOnClickListener{ view ->
            Snackbar.make(view, DUMMY_TEXT, Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }
    }

    private fun initNavigationBar(){
        val bottomNavBar = findViewById<NavigationBarView>(R.id.bottom_navigation)
        bottomNavBar.setOnItemSelectedListener {menuItem ->
            when(menuItem.itemId){
                R.id.items_list_menu_button -> {true}
                R.id.qr_scan_menu_button -> {
                    startActivity(Intent(this, QrCodeScannerActivity::class.java))
                    true
                }
                R.id.chat_menu_button -> {
                    startActivity(Intent(this, ChatMenuActivity::class.java))
                    true
                }
                else -> false
            }
        }
        bottomNavBar.selectedItemId = R.id.items_list_menu_button
    }

    companion object {
        private val customTopBar = CustomTopBar()
    }
}
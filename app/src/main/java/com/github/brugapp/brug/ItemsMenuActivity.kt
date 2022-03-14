package com.github.brugapp.brug

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarItemView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar

private const val DUMMY_TEXT: String = "Actual behavior coming soon…"
private const val SEARCH_HINT: String = "Search items here…"

class ItemsMenuActivity : AppCompatActivity() {
    private val data = ArrayList<ItemsViewModel>()
    private val listViewAdapter = ItemsCustomAdapter(data)

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

        val item = menu?.findItem(R.id.search_box)
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
        val listView = findViewById<RecyclerView>(R.id.items_listview)
        val listUtils = ListUtilities(this, listView, listViewAdapter, data)
        listView.layoutManager = LinearLayoutManager(this)

        data.add(ItemsViewModel(R.drawable.ic_baseline_smartphone_24, "Phone", "Samsung Galaxy S22"))
        data.add(ItemsViewModel(R.drawable.ic_baseline_account_balance_wallet_24, "Wallet", "With all my belongings"))
        data.add(ItemsViewModel(R.drawable.ic_baseline_car_rental_24, "BMW Key", "BMW M3 F80 Competition"))
        data.add(ItemsViewModel(R.drawable.ic_baseline_vpn_key_24, "Keys", "House and everything else"))

        ItemTouchHelper(listUtils.listCallback).attachToRecyclerView(listView)
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
}
package com.github.brugapp.brug

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar

private const val DUMMY_TEXT: String = "Actual behavior coming soon…"
private const val SEARCH_HINT = "Search for a conversation…"
private const val DELETE_TEXT = "Chat feed has been deleted."

class ChatMenuActivity : AppCompatActivity() {

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

    // For the settings icon on top bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        CustomTopBar().defineTopBarActions(window.decorView, DUMMY_TEXT, item)
        return super.onOptionsItemSelected(item)
    }

    private fun initChatList() {
        val listView = findViewById<RecyclerView>(R.id.chat_listview)
        val listViewAdapter = ListCustomAdapter(
            R.layout.chat_entry_layout,
            R.id.chat_entry_profilepic,
            R.id.chat_entry_title,
            R.id.chat_entry_desc
        ) { // HERE TO IMPLEMENT ONCLICK ACTIONS
                clickedItem ->
                Snackbar.make(window.decorView, DUMMY_TEXT, Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()
        }
        val listCallback = ListCustomCallback(this, listViewAdapter, DELETE_TEXT)
        listView.layoutManager = LinearLayoutManager(this)

        listViewAdapter.addEntry(ListViewModel(R.mipmap.ic_launcher, "Anna", "Me: Where are you located ? I'm near the center of Lausanne, so feel free to propose me any location in Lausanne"))
        listViewAdapter.addEntry(ListViewModel(R.mipmap.ic_launcher, "Henry", "Hey ! I might have found your wallet yesterday near the EPFL campus"))
        listViewAdapter.addEntry(ListViewModel(R.mipmap.ic_launcher, "Jenna", "Me: Fine, lets meet on Saturday then !"))
        listViewAdapter.addEntry(ListViewModel(R.mipmap.ic_launcher, "John", "Give me my money back you thief !!!"))

        ItemTouchHelper(listCallback).attachToRecyclerView(listView)
        listView.adapter = listViewAdapter

        listView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
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

}
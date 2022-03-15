package com.github.brugapp.brug

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView

private const val SEARCH_HINT = "Search for a conversation…"

class ChatMenuActivity : AppCompatActivity() {
    private val conversations = ArrayList<ListViewModel>()
    
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
        val listView = findViewById<RecyclerView>(R.id.chat_listview)
        val listViewAdapter = ListCustomAdapter(
            conversations,
            R.layout.chat_entry_layout,
            R.id.chat_entry_profilepic,
            R.id.chat_entry_title,
            R.id.chat_entry_desc
        )
        val listUtils = ListUtilities(this, listView, listViewAdapter, conversations)
        listView.layoutManager = LinearLayoutManager(this)

        conversations.add(ListViewModel(R.mipmap.ic_launcher, "Anna", "Me: Where are you located ? I'm near the center of Lausanne, so feel free to propose me any location in Lausanne"))
        conversations.add(ListViewModel(R.mipmap.ic_launcher, "Henry", "Hey ! I might have found your wallet yesterday near the EPFL campus"))
        conversations.add(ListViewModel(R.mipmap.ic_launcher, "Jenna", "Me: Fine, lets meet on Saturday then !"))
        conversations.add(ListViewModel(R.mipmap.ic_launcher, "John", "Give me my money back you thief !!!"))

        ItemTouchHelper(listUtils.listCallback).attachToRecyclerView(listView)
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
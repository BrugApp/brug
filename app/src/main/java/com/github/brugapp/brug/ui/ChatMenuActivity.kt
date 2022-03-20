package com.github.brugapp.brug.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.*
import com.github.brugapp.brug.ui.components.BottomNavBar
import com.github.brugapp.brug.ui.components.CustomTopBar
import com.github.brugapp.brug.view_model.ChatListAdapter
import com.github.brugapp.brug.view_model.ChatMenuViewModel
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar

private const val DUMMY_TEXT: String = "Actual behavior coming soon…"
private const val SEARCH_HINT: String = "Search for a conversation…"

class ChatMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_menu)

        val model : ChatMenuViewModel by viewModels()

        initChatList(model)
        BottomNavBar().initBottomBar(this)
    }

    // Initializing the top-bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        customTopBar.inflateTopBar(menuInflater, menu, SEARCH_HINT)
        return super.onCreateOptionsMenu(menu)
    }

    // For the settings icon on top bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        customTopBar.defineTopBarActions(window.decorView, DUMMY_TEXT, item)
        return super.onOptionsItemSelected(item)
    }

    private fun initChatList(model: ChatMenuViewModel) {
        val listView = findViewById<RecyclerView>(R.id.chat_listview)
        val listViewAdapter = ChatListAdapter(model.getChatList()) {
            Snackbar.make(window.decorView, DUMMY_TEXT, Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }

        listView.layoutManager = LinearLayoutManager(this)

        ItemTouchHelper(model.ChatListCallback(this, listViewAdapter)).attachToRecyclerView(listView)
        listView.adapter = listViewAdapter

        listView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
    }

    companion object {
        private val customTopBar = CustomTopBar()
    }

}
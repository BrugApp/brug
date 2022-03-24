package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.ui.components.BottomNavBar
import com.github.brugapp.brug.ui.components.CustomTopBar
import com.github.brugapp.brug.view_model.ConversationListAdapter
import com.github.brugapp.brug.view_model.ChatMenuViewModel
import com.github.brugapp.brug.view_model.ListCallback

private const val DUMMY_TEXT: String = "Actual behavior coming soon…"
private const val SEARCH_HINT: String = "Search for a conversation…"
private const val CHECK_TEXT: String = "The conversation has been marked as resolved."


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

        val listViewAdapter = ConversationListAdapter(model.getChatList()) { clickedConv ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("conversation", clickedConv)
            startActivity(intent)
        }

        listView.layoutManager = LinearLayoutManager(this)

        val dragPair = Pair(
            0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
        )

        val swipePair = Pair(
            ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_circle_outline_24)!!,
            ContextCompat.getColor(this, R.color.chat_list_resolve_BG))

        val listAdapterPair = Pair(
            model.getChatList(),
            listViewAdapter
        )

        val listCallback = ListCallback(CHECK_TEXT, dragPair, swipePair, listAdapterPair)
        ItemTouchHelper(listCallback).attachToRecyclerView(listView)
        listView.adapter = listViewAdapter

        listView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
    }

    companion object {
        private val customTopBar = CustomTopBar()
    }

}

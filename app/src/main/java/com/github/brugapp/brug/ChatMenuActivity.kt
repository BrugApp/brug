package com.github.brugapp.brug

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu

private const val SEARCH_HINT = "Search for a conversation…"

class ChatMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_menu)
    }

    // Initializing the top-bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_top_bar, menu)

        val conversation = menu?.findItem(R.id.search_box)
        val searchView = conversation?.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = SEARCH_HINT
        return super.onCreateOptionsMenu(menu)
    }
}
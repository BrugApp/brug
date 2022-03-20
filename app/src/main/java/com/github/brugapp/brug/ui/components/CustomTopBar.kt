package com.github.brugapp.brug

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.google.android.material.snackbar.Snackbar

class CustomTopBar {

    fun inflateTopBar(menuInflater: MenuInflater, menu: Menu?, searchHint: String) {
        menuInflater.inflate(R.menu.custom_top_bar, menu)

        val searchChat = menu?.findItem(R.id.search_box)
        val searchView = searchChat?.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = searchHint
    }

    fun defineTopBarActions(view: View, dummyText: String, item: MenuItem) {
        when(item.itemId){
            R.id.my_settings -> {
                Snackbar.make(view, dummyText, Snackbar.LENGTH_LONG)
                    .show()
            }
            else -> {}
        }
    }
}


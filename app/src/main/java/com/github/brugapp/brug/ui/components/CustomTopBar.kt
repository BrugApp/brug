package com.github.brugapp.brug.ui.components

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.github.brugapp.brug.R
import com.github.brugapp.brug.ui.ChatMenuActivity
import com.github.brugapp.brug.ui.ItemsMenuActivity
import com.github.brugapp.brug.ui.SettingsActivity
import com.google.android.material.snackbar.Snackbar

class CustomTopBar {

    fun inflateTopBar(menuInflater: MenuInflater, menu: Menu?, searchHint: String) {
        menuInflater.inflate(R.menu.custom_top_bar, menu)

        val searchChat = menu?.findItem(R.id.search_box)
        val searchView = searchChat?.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = searchHint
    }

    fun defineTopBarActions(view: View, dummyText: String, item: MenuItem, currentActivity: Activity) {
        when(item.itemId){
            R.id.my_settings -> {
                currentActivity.startActivity(Intent(currentActivity, SettingsActivity::class.java))
            }
            else -> {}
        }
    }
}


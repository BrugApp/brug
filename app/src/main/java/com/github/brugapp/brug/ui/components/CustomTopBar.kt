package com.github.brugapp.brug.ui.components

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.github.brugapp.brug.R
import com.github.brugapp.brug.ui.SettingsActivity

class CustomTopBar {

    fun inflateTopBar(menuInflater: MenuInflater, menu: Menu?) {
        menuInflater.inflate(R.menu.custom_top_bar, menu)
    }

    fun defineTopBarActions(item: MenuItem, currentActivity: Activity) {
        when (item.itemId) {
            R.id.my_settings -> {
                currentActivity.startActivity(Intent(currentActivity, SettingsActivity::class.java))
            }
            else -> {}
        }
    }
}


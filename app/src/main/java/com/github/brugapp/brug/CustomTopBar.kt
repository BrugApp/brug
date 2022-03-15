package com.github.brugapp.brug

import android.view.MenuItem
import android.view.View
import com.google.android.material.snackbar.Snackbar

class CustomTopBar {

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


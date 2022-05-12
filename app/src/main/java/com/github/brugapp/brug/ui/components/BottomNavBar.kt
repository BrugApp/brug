package com.github.brugapp.brug.ui.components

import android.app.Activity
import android.content.Intent
import com.github.brugapp.brug.R
import com.github.brugapp.brug.ui.*
import com.google.android.material.navigation.NavigationBarView

class BottomNavBar {
    fun initBottomBar(currentActivity: Activity) {
        val bottomBar = currentActivity.findViewById<NavigationBarView>(R.id.bottom_navigation)
        when (currentActivity) {
            is ItemsMenuActivity -> bottomBar.selectedItemId = R.id.items_list_menu_button
            is ChatMenuActivity -> bottomBar.selectedItemId = R.id.chat_menu_button
        }


        bottomBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.items_list_menu_button -> {
                    if(currentActivity is ChatMenuActivity || currentActivity is MapBoxActivity){
                        currentActivity.startActivity(Intent(currentActivity, ItemsMenuActivity::class.java))
                    }
                    true
                }
                R.id.qr_scan_menu_button -> {
                    //TODO: HANDLE CASE PROPERLY
                    if(currentActivity is ItemsMenuActivity || currentActivity is ChatMenuActivity){
                        currentActivity.startActivity(Intent(currentActivity, QrCodeScannerActivity::class.java))
                    }
                    true
                }
                R.id.chat_menu_button -> {
                    if(currentActivity is ItemsMenuActivity || currentActivity is MapBoxActivity){
                        currentActivity.startActivity(Intent(currentActivity, ChatMenuActivity::class.java))
                    }
                    true
                }
                R.id.item_map_button -> {
                    if(currentActivity is ItemsMenuActivity || currentActivity is MapBoxActivity){
                        currentActivity.startActivity(Intent(currentActivity, MapBoxActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }
}
package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Item


/**
 * ViewModel of the Items Menu UI, handling its UI logic.
 */
class ItemsMenuViewModel : ViewModel() {
    private val myItemsList: MutableList<Item> by lazy {
        loadItems()
    }

    private fun loadItems(): MutableList<Item> {
        // TODO in the future: Refactor to fetch values from actual database
        return mutableListOf(
            Item("Phone", R.drawable.ic_baseline_smartphone_24, "Samsung Galaxy S22"),
            Item("Wallet", R.drawable.ic_baseline_account_balance_wallet_24, "With all my belongings"),
            Item("BMW Key", R.drawable.ic_baseline_car_rental_24, "BMW M3 F80 Competition"),
            Item("Keys", R.drawable.ic_baseline_vpn_key_24,"House and everything else")
        )
    }

    /**
     * Getter for the list of items.
     */
    fun getItemsList(): MutableList<Item> {
        return myItemsList
    }
}
package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.fake.MockDatabase
import com.github.brugapp.brug.model.Item


/**
 * ViewModel of the Items Menu UI, handling its UI logic.
 */
class ItemsMenuViewModel : ViewModel() {

    /**
     * Getter for the list of items.
     */
    fun getItemsList(): MutableList<Item> {
//        MockDatabase.initUserItems()
        return MockDatabase.currentUser.getItemList()
    }
}
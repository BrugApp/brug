package com.github.brugapp.brug.view_model

import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.ui.ITEMS_DELETE_TEXT
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


private const val ITEMS_ERROR_TEXT = "ERROR: Unable to save your changes remotely. Try again later."

/**
 * ViewModel of the Items Menu UI, handling its UI logic.
 */
class ItemsMenuViewModel : ViewModel() {

    fun setCallback(
        activity: AppCompatActivity,
        isTest: Boolean,
        dragPair: Pair<Int, Int>,
        swipePair: Pair<Drawable, Int>,
        listAdapterPair: Pair<MutableList<Item>, ItemsListAdapter>,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): ListCallback<Item> {

        val listView = activity.findViewById<RecyclerView>(R.id.items_listview)

        return ListCallback(
            ITEMS_DELETE_TEXT,
            dragPair,
            swipePair,
            listAdapterPair
        ) { deletedItem, position ->
            if(!isTest){
                viewModelScope.launch {
                    val result = ItemsRepository.deleteItemFromUser(
                        deletedItem.getItemID(),
                        firebaseAuth.currentUser!!.uid,
                        firestore
                    ).onSuccess

                    if(!result){
                        Toast.makeText(activity, ITEMS_ERROR_TEXT, Toast.LENGTH_LONG).show()
                    }
                }
            }

            listAdapterPair.first.removeAt(position)
            listAdapterPair.second.notifyItemRemoved(position)

            Snackbar.make(
                listView,
                ITEMS_DELETE_TEXT,
                Snackbar.LENGTH_LONG
            ).setAction("Undo") {
                if(!isTest) {
                    viewModelScope.launch{
                        val result = ItemsRepository.addItemToUser(
                            deletedItem,
                            firebaseAuth.currentUser!!.uid,
                            firestore
                        ).onSuccess

                        if(!result){
                            Toast.makeText(activity, ITEMS_ERROR_TEXT, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                listAdapterPair.first.add(position, deletedItem)
                listAdapterPair.second.notifyItemInserted(position)
            }.show()
        }
    }
}
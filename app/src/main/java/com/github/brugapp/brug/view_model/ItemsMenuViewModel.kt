package com.github.brugapp.brug.view_model

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.ui.ITEMS_DELETE_TEXT
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers


/**
 * ViewModel of the Items Menu UI, handling its UI logic.
 */
class ItemsMenuViewModel : ViewModel() {

    fun setCallback(
        activity: AppCompatActivity,
        dragPair: Pair<Int, Int>,
        swipePair: Pair<Drawable, Int>,
        listAdapterPair: Pair<MutableList<MyItem>, ItemsListAdapter>,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): ListCallback<MyItem> {

        val listView = activity.findViewById<RecyclerView>(R.id.items_listview)

        return ListCallback(
            ITEMS_DELETE_TEXT,
            dragPair,
            swipePair,
            listAdapterPair
        ) { deletedItem, position ->
            liveData(Dispatchers.IO) {
                emit(
                    ItemsRepository.deleteItemFromUser(
                        deletedItem.getItemID(), firebaseAuth.currentUser!!.uid, firestore
                    )
                )
            }.observe(activity) { response ->

                if (response.onSuccess) {
                    listAdapterPair.first.removeAt(position)
                    listAdapterPair.second.notifyItemRemoved(position)

                    Snackbar.make(
                        listView,
                        ITEMS_DELETE_TEXT,
                        Snackbar.LENGTH_LONG
                    ).setAction("Undo") {
                        liveData(Dispatchers.IO) {
                            emit(
                                ItemsRepository.addItemToUser(
                                    deletedItem,
                                    firebaseAuth.currentUser!!.uid, firestore
                                )
                            )
                        }.observe(activity) {
                            if (!response.onSuccess) {
                                Snackbar.make(
                                    listView,
                                    "ERROR: Unable to re-add requested item to database",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }

                        listAdapterPair.first.add(position, deletedItem)
                        listAdapterPair.second.notifyItemInserted(position)
                    }.show()
                } else {
                    Snackbar.make(
                        listView,
                        "ERROR: Unable to delete requested item from database",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
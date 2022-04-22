package com.github.brugapp.brug.view_model

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepo
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.ui.ITEMS_DELETE_TEXT
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers


/**
 * ViewModel of the Items Menu UI, handling its UI logic.
 */
class ItemsMenuViewModel : ViewModel() {

    fun setCallback(activity: AppCompatActivity,
                    dragPair: Pair<Int, Int>,
                    swipePair: Pair<Drawable, Int>,
                    listAdapterPair: Pair<MutableList<MyItem>, ItemsListAdapter>): ListCallback<MyItem> {

        val listView = activity.findViewById<RecyclerView>(R.id.items_listview)

        return ListCallback(ITEMS_DELETE_TEXT, dragPair, swipePair, listAdapterPair) { deletedItem, position ->
            liveData(Dispatchers.IO){
                emit(
                    ItemsRepo.deleteItemFromUser(deletedItem.getItemID()
                        , Firebase.auth.currentUser!!.uid))
            }.observe(activity){ response ->

                if(response.onSuccess){
                    listAdapterPair.first.removeAt(position)
                    listAdapterPair.second.notifyItemRemoved(position)

                    Snackbar.make(listView,
                        ITEMS_DELETE_TEXT,
                        Snackbar.LENGTH_LONG).setAction("Undo") {
                        liveData(Dispatchers.IO){
                            emit(
                                ItemsRepo.addItemToUser(deletedItem,
                                    Firebase.auth.currentUser!!.uid))
                        }.observe(activity){
                            if(!response.onSuccess){
                                Snackbar.make(listView,
                                    "ERROR: Unable to re-add requested item to database",
                                    Snackbar.LENGTH_LONG).show()
                            }
                        }

                        listAdapterPair.first.add(position, deletedItem)
                        listAdapterPair.second.notifyItemInserted(position)
                    }.show()
                } else {
                    Snackbar.make(listView,
                        "ERROR: Unable to delete requested item from database",
                        Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
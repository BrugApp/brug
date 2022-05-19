package com.github.brugapp.brug.view_model

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ConvRepository
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.ui.CHAT_CHECK_TEXT
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers


/**
 * ViewModel of the Chat Menu UI, handling its UI logic.
 */
class ChatMenuViewModel : ViewModel() {

    fun setCallback(
        activity: AppCompatActivity,
        isTest: Boolean,
        dragPair: Pair<Int, Int>,
        swipePair: Pair<Drawable, Int>,
        listAdapterPair: Pair<MutableList<Conversation>, ConversationListAdapter>,
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): ListCallback<Conversation> {

        val listView = activity.findViewById<RecyclerView>(R.id.chat_listview)

        return ListCallback(
            CHAT_CHECK_TEXT,
            dragPair,
            swipePair,
            listAdapterPair
        ) { delConv, position ->
            if(isTest){
                listAdapterPair.first.removeAt(position)
                listAdapterPair.second.notifyItemRemoved(position)

                Snackbar.make(
                    listView,
                    CHAT_CHECK_TEXT,
                    Snackbar.LENGTH_LONG
                ).setAction("Undo") {
                    listAdapterPair.first.add(position, delConv)
                    listAdapterPair.second.notifyItemInserted(position)
                }.show()
            } else {
                liveData(Dispatchers.IO) {
                    emit(
                        ConvRepository.deleteConversationFromID(
                            delConv.convId,
                            firebaseAuth.currentUser!!.uid,
                            firestore
                        )
                    )
                }.observe(activity) { response ->
                    if (response.onSuccess) {
                        listAdapterPair.first.removeAt(position)
                        listAdapterPair.second.notifyItemRemoved(position)

                        Snackbar.make(
                            listView,
                            CHAT_CHECK_TEXT,
                            Snackbar.LENGTH_LONG
                        ).setAction("Undo") {
                            liveData(Dispatchers.IO) {
                                emit(
                                    ConvRepository.addNewConversation(
                                        firebaseAuth.currentUser!!.uid,
                                        delConv.userFields.uid,
                                        delConv.lostItem.getItemID(),
                                        firestore
                                    )
                                )
                            }.observe(activity) {
                                if (!response.onSuccess) {
                                    Snackbar.make(
                                        listView,
                                        "ERROR: Unable to re-add requested conversation to database",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            }

                            listAdapterPair.first.add(position, delConv)
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

    private fun callBackActions(){

    }
}
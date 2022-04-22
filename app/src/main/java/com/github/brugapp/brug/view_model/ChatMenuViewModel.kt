package com.github.brugapp.brug.view_model

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ConvRepo
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.ui.CHAT_CHECK_TEXT
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers


/**
 * ViewModel of the Chat Menu UI, handling its UI logic.
 */
class ChatMenuViewModel : ViewModel() {

    fun setCallback(activity: AppCompatActivity,
                    dragPair: Pair<Int, Int>,
                    swipePair: Pair<Drawable, Int>,
                    listAdapterPair: Pair<MutableList<Conversation>, ConversationListAdapter>): ListCallback<Conversation> {

        val listView = activity.findViewById<RecyclerView>(R.id.chat_listview)

        return ListCallback(CHAT_CHECK_TEXT, dragPair, swipePair, listAdapterPair){ delConv, position ->
            liveData(Dispatchers.IO){
                emit(
                    ConvRepo.deleteConversationFromID(delConv.convId, Firebase.auth.currentUser!!.uid))
            }.observe(activity){ response ->
                if(response.onSuccess){
                    listAdapterPair.first.removeAt(position)
                    listAdapterPair.second.notifyItemRemoved(position)

                    Snackbar.make(listView,
                        CHAT_CHECK_TEXT,
                        Snackbar.LENGTH_LONG).setAction("Undo") {
                        liveData(Dispatchers.IO){
                            emit(
                                ConvRepo.addNewConversation(Firebase.auth.currentUser!!.uid, delConv.userFields.uid, delConv.lostItemName)
                            )}.observe(activity){
                            if(!response.onSuccess){
                                Snackbar.make(listView,
                                    "ERROR: Unable to re-add requested conversation to database",
                                    Snackbar.LENGTH_LONG).show()
                            }
                        }

                        listAdapterPair.first.add(position, delConv)
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

    //DON'T KNOW WHAT IS THIS FOR
//        println("===========================================================")
//        println(Uri.parse("content://com.github.brugapp.brug.fileprovider/images/JPEG_20220329_165630_1949197185066641075.jpg"))
//        println("===========================================================")
}
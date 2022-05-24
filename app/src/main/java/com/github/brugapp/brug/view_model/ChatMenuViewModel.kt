package com.github.brugapp.brug.view_model

import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.ConvRepository
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.ui.CHAT_CHECK_TEXT
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

private const val CHAT_ERROR_TEXT = "ERROR: Unable to save your changes remotely. Try again later."

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
            Log.e("CONVDELETECHECK", delConv.lastMessage?.body.toString())
            if(!isTest){
                viewModelScope.launch {
                    val result = ConvRepository.deleteConversationFromID(
                        delConv.convId,
                        firebaseAuth.currentUser!!.uid,
                        firestore
                    ).onSuccess

                    if(!result){
                        Toast.makeText(activity, CHAT_ERROR_TEXT, Toast.LENGTH_LONG).show()
                    }
                }
            }

            listAdapterPair.first.removeAt(position)
            listAdapterPair.second.notifyItemRemoved(position)
            BrugDataCache.deleteCachedMessageList(delConv.convId)

            Snackbar.make(
                listView,
                CHAT_CHECK_TEXT,
                Snackbar.LENGTH_LONG
            ).setAction("Undo") {
                if(!isTest) {
                    val lastMessage = delConv.lastMessage
                    Log.e("CONVUNDOCHECK", lastMessage?.body.toString())
                    viewModelScope.launch {
                        val itemID = "${delConv.userFields.uid}:${delConv.lostItem.getItemID()}"
                        val result = ConvRepository.addNewConversation(
                            firebaseAuth.currentUser!!.uid,
                            delConv.userFields.uid,
                            itemID,
                            delConv.lastMessage,
                            firestore
                        ).onSuccess

                        if(!result){
                            Toast.makeText(activity, CHAT_ERROR_TEXT, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                listAdapterPair.first.add(position, delConv)
                listAdapterPair.second.notifyItemInserted(position)
                BrugDataCache.initMessageListInCache(delConv.convId)
            }.show()
        }
    }
}
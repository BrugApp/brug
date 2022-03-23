package com.github.brugapp.brug.view_model

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.model.ChatMessage
import com.github.brugapp.brug.model.ChatMessagesListAdapter
import com.github.brugapp.brug.ui.ChatActivity
import com.google.firebase.firestore.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatViewModel : ViewModel() {
    private val helper = FirebaseHelper()
    private lateinit var chatArrayList: ArrayList<ChatMessage>
    private lateinit var adapter: ChatMessagesListAdapter

    fun initAdapter() {
        chatArrayList = arrayListOf()
        adapter = ChatMessagesListAdapter(chatArrayList)
    }

    fun getAdapter(): ChatMessagesListAdapter {
        return adapter
    }

    fun eventChangeListener(activity: ChatActivity) {
        // TODO: Change the document when ChatListActivity is implemented
        helper.getMessageCollection("User1","User2")
            .orderBy("datetime", Query.Direction.ASCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                @SuppressLint("NotifyDataSetChanged") // Used by the adapter
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        Log.e("Firestore error", error.message.toString())
                        return
                    }

                    // Add retrieved messages to the list of displayed messages
                    for (dc: DocumentChange in value?.documentChanges!!)
                        if (dc.type == DocumentChange.Type.ADDED) {
                            chatArrayList.add(dc.document.toObject(ChatMessage::class.java))
                        }

                    // Notify the adapter to update the list
                    adapter.notifyDataSetChanged()
                    // TODO: Would be cleaner to have this update inside of ChatActivity
                    //activity.updateData(adapter.itemCount - 1)
                    // Instead I have to do this
                    val rv = activity.findViewById<View>(R.id.recyclerView) as RecyclerView
                    rv.smoothScrollToPosition(adapter.itemCount - 1)
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O) // Used by LocalDateTime
    fun sendMessage(sender: String, content: String) {
        // Compute timestamp
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss")
        val datetime: String = current.format(formatter)

        // Create a new message
        val message = hashMapOf(
            "sender" to sender,
            "content" to content,
            "datetime" to datetime
        )

        // Add a new document i.e. message
        helper.addDocumentMessage("User1","User2",message)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }
}
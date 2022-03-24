package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.ChatMessage
import com.github.brugapp.brug.model.ChatMessagesListAdapter
import java.time.LocalDateTime

class ChatViewModel : ViewModel() {
    private lateinit var messages: MutableList<ChatMessage>
    private lateinit var adapter: ChatMessagesListAdapter

    fun initViewModel(messages: MutableList<ChatMessage>){
        this.messages = messages
        this.adapter = ChatMessagesListAdapter(messages)
    }

    fun getAdapter(): ChatMessagesListAdapter {
        return adapter
    }

    fun sendMessage(content: String){
        val newMessage = ChatMessage(content, LocalDateTime.now(), "Me")
        messages.add(newMessage)
        adapter.notifyItemInserted(messages.size-1)
    }



/* OLD IMPLEMENTATION USING FIREBASE -> TO BE REFACTORED PROPERLY AFTERWARDS
    // TODO: Have to be removed when the data.DataBase class is implemented
    private lateinit var db: FirebaseFirestore

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
        // TODO: Update code to use data.Database when implemented
        db = Firebase.firestore
        db.collection("Chat").document("User1User2").collection("Messages")
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
        db = Firebase.firestore
        db.collection("Chat").document("User1User2")
            .collection("Messages")
            .add(message)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }


    */
}
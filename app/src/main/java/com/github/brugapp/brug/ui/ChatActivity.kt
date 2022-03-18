package com.github.brugapp.brug

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatArrayList: ArrayList<ChatItemModel>
    private lateinit var adapter: ChatMessagesListAdapter
    private lateinit var db: FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // ACTUAL CHAT PART
        initMessageList()
        eventChangeListener()

        // SEND BUTTON
        val buttonSendMessage = findViewById<Button>(R.id.buttonSendMessage)
        buttonSendMessage.setOnClickListener { sendMessage() }

        // NAVIGATION BAR
        initNavigationBar()
    }

    private fun initMessageList(){
        recyclerView = findViewById(R.id.recyclerView)
        val linearManager: LinearLayoutManager = LinearLayoutManager(this)
        linearManager.stackFromEnd = true
        recyclerView.layoutManager = linearManager
        recyclerView.setHasFixedSize(true)

        chatArrayList = arrayListOf()
        adapter = ChatMessagesListAdapter(chatArrayList)

        recyclerView.adapter = adapter
    }

    @SuppressLint("CutPasteId")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessage() {
        // Get elements of the message
        val sender: String = this.findViewById<TextView?>(R.id.editName).text.toString()
        val content: String = this.findViewById<TextView?>(R.id.editMessage).text.toString()
        this.findViewById<TextView?>(R.id.editMessage).text = ""

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
        db = FirebaseFirestore.getInstance()
        // Firebase.firestore change for kotlin?
        db.collection("Chat").document("User1User2")
            .collection("Messages")
            .add(message)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun eventChangeListener() {
        db = FirebaseFirestore.getInstance()
        // TODO: Change the document when ChatListActivity is implemented
        db.collection("Chat").document("User1User2").collection("Messages")
            .orderBy("datetime", Query.Direction.ASCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                @SuppressLint("NotifyDataSetChanged")
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
                            chatArrayList.add(dc.document.toObject(ChatItemModel::class.java))
                        }

                    // Notify the adapter to update the list
                    adapter.notifyDataSetChanged()
                    val rv = findViewById<View>(R.id.recyclerView) as RecyclerView
                    rv.smoothScrollToPosition(adapter.itemCount - 1)
                }
            })
    }

    // Setup the navigation bar
    private fun initNavigationBar() {
        val bottomNavBar = findViewById<NavigationBarView>(R.id.bottom_navigation)
        bottomNavBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.items_list_menu_button -> {
                    startActivity(Intent(this, ItemsMenuActivity::class.java))
                    true
                }
                R.id.qr_scan_menu_button -> {
                    startActivity(Intent(this, QrCodeScannerActivity::class.java))
                    true
                }
                R.id.chat_menu_button -> {
                    true
                }
                else -> false
            }
        }
        bottomNavBar.selectedItemId = R.id.chat_menu_button
    }
}
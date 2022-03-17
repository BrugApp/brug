package com.github.brugapp.brug

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.firestore.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatArrayList: ArrayList<ChatItemModel>
    private lateinit var adapter: ChatItemAdapter
    private lateinit var db: FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // ACTUAL CHAT PART
        recyclerView = findViewById(R.id.recyclerView)
        val linearManager: LinearLayoutManager = LinearLayoutManager(this)
        linearManager.stackFromEnd = true
        recyclerView.layoutManager = linearManager
        recyclerView.setHasFixedSize(true)

        chatArrayList = arrayListOf()
        adapter = ChatItemAdapter(chatArrayList)

        recyclerView.adapter = adapter

        eventChangeListener()

        // SEND BUTTON
        val buttonSendMessage = findViewById<Button>(R.id.buttonSendMessage)
        buttonSendMessage.setOnClickListener{ sendMessage() }

        // NAVIGATION BAR
        initNavigationBar()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessage() {
        // Get elements of the message
        val sender : String = this.findViewById<TextView?>(R.id.editName).text.toString()
        val content : String = this.findViewById<TextView?>(R.id.editMessage).text.toString()
        this.findViewById<TextView?>(R.id.editMessage).text = ""

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss")
        val datetime : String = current.format(formatter)

        // Create a new message
        val message = hashMapOf(
            "sender" to sender,
            "content" to content,
            "datetime" to datetime
        )

        // Add a new document with a generated ID
        db = FirebaseFirestore.getInstance()
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
        db.collection("Chat").document("User1User2").collection("Messages")
            .orderBy("datetime", Query.Direction.ASCENDING).addSnapshotListener(object : EventListener<QuerySnapshot>{
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null){
                    Log.e("Firestore error", error.message.toString())
                    return
                }

                for (dc : DocumentChange in value?.documentChanges!!)
                    if(dc.type == DocumentChange.Type.ADDED){
                        chatArrayList.add(dc.document.toObject(ChatItemModel::class.java))
                        println(dc.document.toObject(ChatItemModel::class.java).datetime)
                    }

                adapter.notifyDataSetChanged()
                val rv = findViewById<View>(R.id.recyclerView) as RecyclerView
                rv.smoothScrollToPosition(adapter.itemCount - 1)
            }

        })
    }

    private fun initNavigationBar(){
        val bottomNavBar = findViewById<NavigationBarView>(R.id.bottom_navigation)
        bottomNavBar.setOnItemSelectedListener {menuItem ->
            when(menuItem.itemId){
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
package com.github.brugapp.brug

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.firestore.*

class ChatActivity : AppCompatActivity() {
    companion object {
        const val REPLACE_COLLECTION_KEY = "Chat"
        const val REPLACE_DOCUMENT_KEY = "Message"
        const val REPLACE_NAME_FIELD = "Name"
        const val REPLACE_TEXT_FIELD = "Text"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatArrayList: ArrayList<ChatItemModel>
    private lateinit var adapter: ChatItemAdapter
    private lateinit var db: FirebaseFirestore

    private val firestoreChatReplace by lazy {
        FirebaseFirestore.getInstance().collection(REPLACE_COLLECTION_KEY).document(REPLACE_DOCUMENT_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // ACTUAL CHAT PART
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        chatArrayList = arrayListOf()
        adapter = ChatItemAdapter(chatArrayList)

        recyclerView.adapter = adapter

        eventChangeListener()

        // REPLACE MESSAGING PART
        realtimeUpdateListener()

        val buttonSendMessage = findViewById<Button>(R.id.buttonSendMessage)
        buttonSendMessage.setOnClickListener{ sendMessage() }

        // NAVIGATION BAR
        initNavigationBar()
    }

    private fun sendMessage() {
        val edit_name = findViewById<EditText>(R.id.editName)
        val edit_message = findViewById<EditText>(R.id.editMessage)

        val newMessage = mapOf(
            REPLACE_NAME_FIELD to edit_name.text.toString(),
            REPLACE_TEXT_FIELD to edit_message.text.toString())
        firestoreChatReplace.set(newMessage)
            .addOnSuccessListener {
                Toast.makeText(this@ChatActivity, "Message Sent", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> e.message?.let { Log.e("ERROR", it) } }
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
            }

        })
    }

    private fun realtimeUpdateListener() {
        firestoreChatReplace.addSnapshotListener { documentSnapshot, e ->
            when {
                e != null -> e.message?.let { Log.e("ERROR", it) }
                documentSnapshot != null && documentSnapshot.exists() -> {
                    with(documentSnapshot) {
                        val textDisplay = findViewById<TextView>(R.id.textDisplay)
                        textDisplay.text = "${data?.get(REPLACE_NAME_FIELD)}:${data?.get(REPLACE_TEXT_FIELD)}"
                    }
                }
            }
        }
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
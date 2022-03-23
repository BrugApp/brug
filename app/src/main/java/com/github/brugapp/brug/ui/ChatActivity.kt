package com.github.brugapp.brug.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.ChatViewModel
import com.google.android.gms.location.FusedLocationProviderClient

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView


    @SuppressLint("CutPasteId") // Needed as we read values from EditText fields
    @RequiresApi(Build.VERSION_CODES.O) // Needed for the adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // ACTUAL CHAT PART
        initMessageList()
        viewModel.eventChangeListener(this)

        // SEND MESSAGE BUTTON
        val buttonSendMessage = findViewById<ImageButton>(R.id.buttonSendMessage)
        buttonSendMessage.setOnClickListener {
            // Get elements from UI
            val sender: String = this.findViewById<TextView?>(R.id.editName).text.toString()
            val content: String = this.findViewById<TextView?>(R.id.editMessage).text.toString()
            // Clear the message field
            this.findViewById<TextView?>(R.id.editMessage).text = ""
            viewModel.sendMessage(sender, content)
        }

        // SEND LOCALISATION BUTTON
        val buttonSendLocalisation = findViewById<ImageButton>(R.id.buttonSendLocalisation)
        buttonSendLocalisation.setOnClickListener {
            viewModel.sendLocalisation(this)
        }
    }

    private fun initMessageList() {
        recyclerView = findViewById(R.id.recyclerView)
        val linearManager: LinearLayoutManager = LinearLayoutManager(this)
        linearManager.stackFromEnd = true
        recyclerView.layoutManager = linearManager
        recyclerView.setHasFixedSize(true)

        viewModel.initAdapter()
        recyclerView.adapter = viewModel.getAdapter()
    }

    fun updateData(numberOfMessages: Int) {
        val rv = findViewById<View>(R.id.recyclerView) as RecyclerView
        rv.smoothScrollToPosition(numberOfMessages - 1)
    }
}
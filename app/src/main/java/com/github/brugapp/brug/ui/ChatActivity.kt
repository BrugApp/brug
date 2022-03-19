package com.github.brugapp.brug.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.ChatViewModel

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView

    @SuppressLint("CutPasteId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // ACTUAL CHAT PART
        initMessageList()
        viewModel.eventChangeListener(this)

        // SEND BUTTON
        val buttonSendMessage = findViewById<Button>(R.id.buttonSendMessage)
        buttonSendMessage.setOnClickListener {
            // Get elements from UI
            val sender: String = this.findViewById<TextView?>(R.id.editName).text.toString()
            val content: String = this.findViewById<TextView?>(R.id.editMessage).text.toString()
            // Clear the message field
            this.findViewById<TextView?>(R.id.editMessage).text = ""
            viewModel.sendMessage(sender, content)
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
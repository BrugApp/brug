package com.github.brugapp.brug.ui

import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.view_model.ChatViewModel

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val model: ChatViewModel by viewModels()

        initMessageList(model)
        initSendButton(model)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun initMessageList(model: ChatViewModel) {
        val conversation: Conversation = intent.getSerializableExtra("conversation") as Conversation

        val messageList = findViewById<RecyclerView>(R.id.recyclerView)

        model.initViewModel(conversation.messages)

        messageList.layoutManager = LinearLayoutManager(this)
        messageList.adapter = model.getAdapter()

        // Set the title bar name with informations related to the conversation
        inflateActionBar(
            "${conversation.user.getFirstName()} ${conversation.user.getLastName()}",
            conversation.lostItem.getName()
        )
    }

    private fun inflateActionBar(username: String, itemLost: String){
        supportActionBar!!.title = username
        supportActionBar!!.subtitle = "Related to your item \"$itemLost\""
    }

    private fun initSendButton(model: ChatViewModel){
        val buttonSendMessage = findViewById<Button>(R.id.buttonSendMessage)
        buttonSendMessage.setOnClickListener {
            // Get elements from UI
            val content: String = findViewById<TextView>(R.id.editMessage).text.toString()
            // Clear the message field
            this.findViewById<TextView>(R.id.editMessage).text = ""
            model.sendMessage(content)
        }
    }
}
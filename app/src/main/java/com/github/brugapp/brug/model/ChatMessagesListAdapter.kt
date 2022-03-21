package com.github.brugapp.brug.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R

// Adapter that binds the list of messages to the instances of ChatItemModel
class ChatMessagesListAdapter(private val messageList: ArrayList<ChatMessage>) :
    RecyclerView.Adapter<ChatMessagesListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message: ChatMessage = messageList[position]
        holder.sender.text = message.sender
        holder.content.text = message.content
        holder.datetime.text = message.datetime
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    public class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sender: TextView = itemView.findViewById(R.id.chat_item_sender)
        val datetime: TextView = itemView.findViewById(R.id.chat_item_datetime)
        val content: TextView = itemView.findViewById(R.id.chat_item_content)
    }
}
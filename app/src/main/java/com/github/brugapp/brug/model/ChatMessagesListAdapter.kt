package com.github.brugapp.brug.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Adapter that binds the list of messages to the instances of ChatItemModel
class ChatMessagesListAdapter(private val messageList: MutableList<Message>) :
    RecyclerView.Adapter<ChatMessagesListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message: Message = messageList[position]
        holder.sender.text = message.sender
        holder.content.text = message.body
        holder.datetime.text = formatDateTime(message.timestamp)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    private fun formatDateTime(date: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy - HH:mm")
        return date.format(formatter)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sender: TextView = itemView.findViewById(R.id.chat_item_sender)
        val datetime: TextView = itemView.findViewById(R.id.chat_item_datetime)
        val content: TextView = itemView.findViewById(R.id.chat_item_content)
    }
}
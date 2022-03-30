package com.github.brugapp.brug.model

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Adapter that binds the list of messages to the instances of ChatItemModel
class ChatMessagesListAdapter(private val messageList: MutableList<Message>) :
    RecyclerView.Adapter<ChatMessagesListAdapter.ViewHolder>() {

    companion object {
        private const val TYPE_MESSAGE = 0
        private const val TYPE_IMAGE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            TYPE_MESSAGE -> R.layout.chat_item_layout
            TYPE_IMAGE -> R.layout.chat_image_layout
            else -> throw IllegalArgumentException("Invalid type")
        }

        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(itemView)
    }

    // Bind view with data models
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messageList[position])
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (messageList[position]) {
            is ChatMessage -> TYPE_MESSAGE
            // is ChatImage -> TYPE_IMAGE // Is the template for other kinds of messages
            else -> TYPE_IMAGE
        }
    }

    fun setData(m: List<Message>) {
        messageList.apply {
            clear()
            addAll(m)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private fun formatDateTime(date: LocalDateTime): String {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yy - HH:mm")
            return date.format(formatter)
        }

        private fun bindChatMessage(message: ChatMessage) {
            itemView.findViewById<TextView>(R.id.chat_item_sender).text = message.sender
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text = formatDateTime(message.timestamp)
            itemView.findViewById<TextView>(R.id.chat_item_content).text = message.body
        }

        private fun bindChatImage(message: ChatImage) {
            itemView.findViewById<TextView>(R.id.chat_image_sender).text = message.sender
            itemView.findViewById<ImageView>(R.id.chat_image_imageView).setImageURI(Uri.parse(message.imageURI))
        }

        fun bind(messageModel: Message) {
            when (messageModel) {
                is ChatMessage -> bindChatMessage(messageModel)
                is ChatImage -> bindChatImage(messageModel)
            }
        }
    }
}
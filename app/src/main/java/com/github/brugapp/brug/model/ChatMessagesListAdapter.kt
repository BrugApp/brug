package com.github.brugapp.brug.model

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.MessageResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Adapter that binds the list of messages to the instances of ChatItemModel
class ChatMessagesListAdapter(private val messageList: MutableList<MessageResponse>) :
    RecyclerView.Adapter<ChatMessagesListAdapter.ViewHolder>() {

    companion object {
        private const val ERROR = -1
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
        val messageResponse = messageList[position]
        if(messageResponse.onError != null){
            Log.e("Firebase error", messageResponse.onError.toString())
        } else {
            holder.bind(messageResponse.onSuccess!!)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val messageResponse = messageList[position]

        return if(messageResponse.onError != null){
            Log.e("Firebase error", messageResponse.onError.toString())
            ERROR
        } else {
            when(messageResponse.onSuccess!!){
                is PicMessage -> TYPE_IMAGE
                else -> TYPE_MESSAGE
            }
        }
    }

//    fun setData(m: List<Message>) {
//        messageList.apply {
//            clear()
//            addAll(m)
//        }
//    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private fun formatDateTime(date: LocalDateTime): String {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yy - HH:mm")
            return date.format(formatter)
        }

        private fun bindTextMessage(message: Message) {
            itemView.findViewById<TextView>(R.id.chat_item_sender).text = message.sender
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text = formatDateTime(message.timestamp)
            itemView.findViewById<TextView>(R.id.chat_item_content).text = message.body
        }

        private fun bindPicMessage(message: PicMessage) {
            itemView.findViewById<TextView>(R.id.chat_image_sender).text = message.sender
            itemView.findViewById<ImageView>(R.id.chat_image_imageView).setImageURI(Uri.parse(message.imgUrl))
        }

        fun bind(messageModel: Message) {
            when (messageModel) {
                is PicMessage -> bindPicMessage(messageModel)
                else -> bindTextMessage(messageModel)
            }
        }
    }
}
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
import com.github.brugapp.brug.model.ChatMessagesListAdapter.MessageType.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Adapter that binds the list of messages to the instances of ChatItemModel
class ChatMessagesListAdapter(private val messageList: MutableList<MessageResponse>) :
    RecyclerView.Adapter<ChatMessagesListAdapter.ViewHolder>() {

    enum class MessageType {
        TYPE_MESSAGE, TYPE_IMAGE, TYPE_AUDIO, TYPE_LOCATION
    }

//    companion object {
//        private const val ERROR = -1
//        private const val TYPE_MESSAGE = 0
//        private const val TYPE_IMAGE = 1
//        private const val TYPE_LOCATION = 2
//    }

    //TODO: CHANGE TO CORRECT LAYOUT WHEN IMPLEMENTED
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            TYPE_MESSAGE.ordinal -> R.layout.chat_item_layout
            TYPE_IMAGE.ordinal -> R.layout.chat_image_layout
            TYPE_AUDIO.ordinal -> R.layout.chat_image_layout
            TYPE_LOCATION.ordinal -> R.layout.chat_item_layout
            else -> throw IllegalArgumentException("Invalid type")
        }

        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(itemView)
    }

    // Bind view with data models
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val messageResponse = messageList[position]
        if(messageResponse.onSuccess == null){
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
            -1
        } else {
            when(messageResponse.onSuccess!!){
                is AudioMessage -> TYPE_AUDIO.ordinal
                is LocationMessage -> TYPE_LOCATION.ordinal
                is PicMessage -> TYPE_IMAGE.ordinal
                else -> TYPE_MESSAGE.ordinal
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

        private fun bindLocationMessage(message: LocationMessage) {
            itemView.findViewById<TextView>(R.id.chat_item_sender).text = message.sender
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text = formatDateTime(message.timestamp)
            itemView.findViewById<TextView>(R.id.chat_item_content).text = message.body
        }

        private fun bindPicMessage(message: PicMessage) {
            itemView.findViewById<TextView>(R.id.chat_image_sender).text = message.sender
            itemView.findViewById<ImageView>(R.id.chat_image_imageView).setImageURI(Uri.parse(message.imgUrl))
        }

        private fun bindAudioMessage(message: AudioMessage) {
            itemView.findViewById<TextView>(R.id.chat_image_sender).text = message.sender
            itemView.findViewById<ImageView>(R.id.chat_image_imageView).setImageURI(Uri.parse(message.audioUrl))
        }

        private fun bindTextMessage(message: Message) {
            itemView.findViewById<TextView>(R.id.chat_item_sender).text = message.sender
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text = formatDateTime(message.timestamp)
            itemView.findViewById<TextView>(R.id.chat_item_content).text = message.body
        }

        fun bind(messageModel: Message) {
            when (messageModel) {
                is LocationMessage -> bindLocationMessage(messageModel)
                is PicMessage -> bindPicMessage(messageModel)
                is AudioMessage -> bindAudioMessage(messageModel)
                else -> bindTextMessage(messageModel)
            }
        }
    }
}
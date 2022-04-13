package com.github.brugapp.brug.model

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.ChatMessagesListAdapter.MessageType.*
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Adapter that binds the list of messages to the instances of ChatItemModel
class ChatMessagesListAdapter(private val messageList: MutableList<Message>) :
    RecyclerView.Adapter<ChatMessagesListAdapter.ViewHolder>() {

    enum class MessageType {
        TYPE_MESSAGE_RIGHT, TYPE_MESSAGE_LEFT, TYPE_IMAGE_RIGHT, TYPE_IMAGE_LEFT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            TYPE_MESSAGE_RIGHT.ordinal -> R.layout.chat_item_layout_right
            TYPE_MESSAGE_LEFT.ordinal -> R.layout.chat_item_layout_left
            TYPE_IMAGE_RIGHT.ordinal -> R.layout.chat_image_layout_right
            TYPE_IMAGE_LEFT.ordinal -> R.layout.chat_image_layout_left
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
        val message: Message = messageList[position]
        return if(message.senderName == "Me"){
            if(message is PicMessage) TYPE_IMAGE_LEFT.ordinal
            else TYPE_MESSAGE_LEFT.ordinal
        } else {
            if(message is PicMessage) TYPE_IMAGE_RIGHT.ordinal
            else TYPE_MESSAGE_RIGHT.ordinal
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
            itemView.findViewById<ImageView>(R.id.picture).setImageResource(R.mipmap.ic_launcher)
            itemView.findViewById<TextView>(R.id.chat_item_sender).text = message.senderName
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text =
                formatDateTime(message.timestamp.toLocalDateTime())

            itemView.findViewById<TextView>(R.id.chat_item_content).text = message.body
            itemView.findViewById<ImageView>(R.id.picture)
                .setImageResource(R.drawable.ic_launcher_background)
        }

        private fun bindPicMessage(message: PicMessage) {
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text =
                formatDateTime(message.timestamp.toLocalDateTime())
            itemView.findViewById<ImageView>(R.id.picture).setImageURI(Uri.parse(message.imgUrl))
        }



        //TODO: CHANGE BINDINGS WHEN LAYOUTS ARE IMPLEMENTED
        fun bind(messageModel: Message) {
            when (messageModel) {
                is LocationMessage -> bindTextMessage(messageModel)
                is PicMessage -> bindPicMessage(messageModel)
                is AudioMessage -> bindTextMessage(messageModel)
                else -> bindTextMessage(messageModel)
            }
        }
    }
}
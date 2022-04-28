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
    // for onclick on the items of the recycler
    private lateinit var mListener: onItemClickListener
    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    enum class MessageType {
        TYPE_MESSAGE_RIGHT, TYPE_MESSAGE_LEFT, TYPE_IMAGE_RIGHT, TYPE_IMAGE_LEFT, TYPE_LOCATION_RIGHT, TYPE_LOCATION_LEFT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            TYPE_MESSAGE_RIGHT.ordinal -> R.layout.chat_item_layout_right
            TYPE_MESSAGE_LEFT.ordinal -> R.layout.chat_item_layout_left
            TYPE_IMAGE_RIGHT.ordinal -> R.layout.chat_image_layout_right
            TYPE_IMAGE_LEFT.ordinal -> R.layout.chat_image_layout_left
            TYPE_LOCATION_LEFT.ordinal -> R.layout.chat_location_layout_left
            TYPE_LOCATION_RIGHT.ordinal -> R.layout.chat_location_layout_right
            else -> throw IllegalArgumentException("Invalid type")
        }

        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(itemView, mListener)
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
        return if (message.senderName == "Me") {
            if (message is PicMessage) TYPE_IMAGE_LEFT.ordinal
            else if (message is LocationMessage) TYPE_LOCATION_LEFT.ordinal
            else TYPE_MESSAGE_LEFT.ordinal
        } else {
            if (message is PicMessage) TYPE_IMAGE_RIGHT.ordinal
            else if (message is LocationMessage) TYPE_LOCATION_RIGHT.ordinal
            else TYPE_MESSAGE_RIGHT.ordinal
        }
    }

    class ViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

        private fun formatDateTime(date: LocalDateTime): String {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yy - HH:mm")
            return date.format(formatter)
        }

        private fun bindTextMessage(message: Message) {
            itemView.findViewById<ImageView>(R.id.picture)
                .setImageResource(R.mipmap.ic_launcher)
            itemView.findViewById<TextView>(R.id.chat_item_sender).text = message.senderName
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text =
                formatDateTime(message.timestamp.toLocalDateTime())
            itemView.findViewById<TextView>(R.id.chat_item_content).text = message.body
        }

        private fun bindPicMessage(message: PicMessage) {
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text =
                formatDateTime(message.timestamp.toLocalDateTime())
            itemView.findViewById<ImageView>(R.id.picture)
                .setImageURI(Uri.parse(message.imgUrl))
        }

        private fun bindLocationMessage(message: LocationMessage) {
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text =
                formatDateTime(message.timestamp.toLocalDateTime())
            itemView.findViewById<ImageView>(R.id.map).setImageURI(Uri.parse(message.mapUrl))
        }

        fun bind(messageModel: Message) {
            when (messageModel) {
                is LocationMessage -> bindLocationMessage(messageModel)
                is PicMessage -> bindPicMessage(messageModel)
                is AudioMessage -> bindTextMessage(messageModel)
                else -> bindTextMessage(messageModel)
            }
        }
    }
}
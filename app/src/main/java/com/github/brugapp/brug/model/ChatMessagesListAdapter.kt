package com.github.brugapp.brug.model

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.ChatMessagesListAdapter.MessageType.*
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.view_model.ChatViewModel
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Adapter that binds the list of messages to the instances of ChatItemModel
/**
 * Adapter that binds the list of messages to the list of views according to the ChatViewModel
 *
 * @property viewModel the model of messages
 * @property messageList the list of messages to bind
 */
class ChatMessagesListAdapter(
    private val viewModel: ChatViewModel,
    private val messageList: MutableList<Message>
) :
    RecyclerView.Adapter<ChatMessagesListAdapter.ViewHolder>() {
    // for onclick on the items of the recycler
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    /**
     * Set a listener on a particular item
     *
     * @param listener the listener on the item
     */
    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    /**
     * Enum Class that defines the different kinds of messages
     * We need two types per kind of messages as they can be displayed on the right/left of the screen
     */
    enum class MessageType {
        TYPE_MESSAGE_RIGHT, TYPE_MESSAGE_LEFT, TYPE_IMAGE_RIGHT, TYPE_IMAGE_LEFT, TYPE_LOCATION_RIGHT, TYPE_LOCATION_LEFT, TYPE_AUDIO_LEFT, TYPE_AUDIO_RIGHT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            TYPE_MESSAGE_LEFT.ordinal -> R.layout.chat_item_layout_left
            TYPE_MESSAGE_RIGHT.ordinal -> R.layout.chat_item_layout_right
            TYPE_IMAGE_LEFT.ordinal -> R.layout.chat_image_layout_left
            TYPE_IMAGE_RIGHT.ordinal -> R.layout.chat_image_layout_right
            TYPE_LOCATION_LEFT.ordinal -> R.layout.chat_location_layout_left
            TYPE_LOCATION_RIGHT.ordinal -> R.layout.chat_location_layout_right
            TYPE_AUDIO_LEFT.ordinal -> R.layout.left_audio_layout
            TYPE_AUDIO_RIGHT.ordinal -> R.layout.right_audio_layout
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
            when (message) {
                is PicMessage -> TYPE_IMAGE_RIGHT.ordinal
                is LocationMessage -> TYPE_LOCATION_RIGHT.ordinal
                is AudioMessage -> TYPE_AUDIO_RIGHT.ordinal
                else -> TYPE_MESSAGE_RIGHT.ordinal
            }
        } else {
            when (message) {
                is PicMessage -> TYPE_IMAGE_LEFT.ordinal
                is LocationMessage -> TYPE_LOCATION_LEFT.ordinal
                is AudioMessage -> TYPE_AUDIO_LEFT.ordinal
                else -> TYPE_MESSAGE_LEFT.ordinal
            }
        }
    }

    /**
     * Getter for a message at a given position
     *
     * @param position the index of the message in the RecyclerView
     * @return the message at the provided position
     */
    fun getItem(position: Int): Message{
        return messageList[position]
    }

    /**
     * Class used to bind the messages to the elements of the RecyclerView
     *
     * @param itemView The itemView to populate
     * @param listener The listener that will be notified in case of changes (add a new message)
     */
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
            itemView.findViewById<TextView>(R.id.chat_item_sender).text = message.senderName
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text =
                formatDateTime(message.timestamp.toLocalDateTime())
            itemView.findViewById<TextView>(R.id.chat_item_content).text = message.body
        }

        private fun bindPicMessage(message: PicMessage) {
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text =
                formatDateTime(message.timestamp.toLocalDateTime())

            // set image and width/height (keeping ratio)
            val imageView = itemView.findViewById<ImageView>(R.id.picture)
            val (uri, width, height) = resizeImage(Uri.parse(message.imgUrl))
            imageView.setImageURI(uri)
            imageView.maxHeight = height
            imageView.maxWidth = width
        }

        private fun bindLocationMessage(message: LocationMessage) {
            itemView.findViewById<TextView>(R.id.chat_item_datetime).text =
                formatDateTime(message.timestamp.toLocalDateTime())
            message.getImageUri().observeForever { uri ->
                itemView.findViewById<ImageView>(R.id.map).setImageURI(uri)
            }
        }

        private fun bindAudioMessage(message: AudioMessage) {
            itemView.findViewById<VoicePlayerView>(R.id.voicePlayerView).setAudio(message.audioPath)
        }

        private fun resizeImage(uri: Uri): Triple<Uri, Int, Int> {
            // open the image
            val image = Drawable.createFromPath(uri.path)
            val imageBM = image!!.toBitmap(
                image.intrinsicWidth,
                image.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )

            // resize it
            val (width, height) = ChatViewModel().computeWidthHeight(imageBM.width, imageBM.height, 500, 500)
            val resized = Bitmap.createScaledBitmap(imageBM, width, height, false)

            // store to new file
            val newFile = File.createTempFile("temp", ".jpg")
            val outputStream = FileOutputStream(newFile)
            resized.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            // return uri of new file
            return Triple(Uri.fromFile(newFile), width, height)
        }

        /**
         * Binds messages to the View
         *
         * @param messageModel the message that will be binded
         */
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
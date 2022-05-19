package com.github.brugapp.brug.view_model

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Conversation

/**
 * Custom adapter class for the RecyclerView lists in ChatMenuActivity
 */

private const val USERPIC_LEN = 192

class ConversationListAdapter(
    private val chatList: MutableList<Conversation>,
    private val onItemClicked: (Conversation) -> Unit
) : RecyclerView.Adapter<ListViewHolder>() {


    // Creates new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_entry_layout, parent, false)
        return ListViewHolder(
            view,
            R.id.chat_entry_profilepic,
            R.id.chat_entry_title,
            R.id.chat_entry_desc
        ) {
            onItemClicked(chatList[it])
        }
    }

    // Binds the list items to a view
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listElement = chatList[position]
        holder.title.text = listElement.userFields.getFullName()
        if(listElement.userFields.getUserIconPath() == null){
            holder.icon.setImageResource(R.mipmap.ic_launcher)
        } else {
            val drawableIcon = Drawable.createFromPath(listElement.userFields.getUserIconPath())
            val bitmap = drawableIcon!!.toBitmap(USERPIC_LEN, USERPIC_LEN, Bitmap.Config.ARGB_8888)
            holder.icon.setImageBitmap(bitmap)
        }
        val lastMessage = listElement.lastMessage

        val lastMessageSender =
            if(lastMessage == null) ""
            else if (lastMessage.senderName == listElement.userFields.uid) "${listElement.userFields.getFullName()}:"
            else "Me:"

        val lastMessageBody =
            if (lastMessage == null) "Empty Conversation"
            else {
                "$lastMessageSender ${lastMessage.body}"
            }

        holder.desc.text = lastMessageBody
    }

    // private fun uriToDrawable(uriString: String?): Drawable {
    //     val uri = Uri.parse(uriString)
    //     val inputStream = activity.contentResolver.openInputStream(uri)
    //     return Drawable.createFromStream(inputStream, uri.toString())
    // }

    // Returns the number of elements in the list
    override fun getItemCount(): Int {
        return chatList.size
    }

}
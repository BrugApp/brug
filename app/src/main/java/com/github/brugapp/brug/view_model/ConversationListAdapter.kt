package com.github.brugapp.brug.view_model

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Conversation

/**
 * Custom adapter class for the RecyclerView lists in ChatMenuActivity
 */
class ConversationListAdapter(
    private val chatList: MutableList<Conversation>,
    private val onItemClicked: (Conversation) -> Unit
) : RecyclerView.Adapter<ListViewHolder>() {

    // Creates new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_entry_layout, parent, false)
        return ListViewHolder(view, R.id.chat_entry_profilepic, R.id.chat_entry_title, R.id.chat_entry_desc) {
            onItemClicked(chatList[it])
        }
    }

    // Binds the list items to a view
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listElement = chatList[position]
        holder.title.text = listElement.userFields.getFullName()
        if(listElement.userFields.iconPath.isNullOrBlank()){
            holder.icon.setImageResource(R.mipmap.ic_launcher)
        } else {
            holder.icon.setImageURI(Uri.parse(listElement.userFields.iconPath))
        }
        val lastMessage = listElement.messages.last()
        val lastMessageBody = "${lastMessage.senderName}: ${lastMessage.body}"
        holder.desc.text = lastMessageBody
    }

    // Returns the number of elements in the list
    override fun getItemCount(): Int {
        return chatList.size
    }

}
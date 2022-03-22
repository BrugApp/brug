package com.github.brugapp.brug.view_model

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Conversation

/**
 * Custom adapter class for the RecyclerView lists in ChatMenuActivity
 */
class ChatListAdapter(
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
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listElement = chatList[position]

        // TODO: replace the hardcoded image ID by the ID of the profile-pic of user
        val lastMessage = listElement.messages.last()
        holder.icon.setImageResource(R.mipmap.ic_launcher)
        holder.title.text = listElement.user.getFirstName()
        holder.desc.text = "${lastMessage.sender}: ${lastMessage.content}"

    }

    // Returns the number of elements in the list
    override fun getItemCount(): Int {
        return chatList.size
    }

    /**
     * Getter for the list of conversations.
     */
    fun getChatList(): MutableList<Conversation> {
        return chatList
    }

}
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

    // Better way would be to have a format string in the strings.xml,
    // but it seems too complicated (I wasn't able to use the proper methods
    // to populate the slots + complicates the implementation for nothing)
    @SuppressLint("SetTextI18n")
    // Binds the list items to a view
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listElement = chatList[position]

        // TODO: replace the hardcoded image ID by the ID of the profile-pic of user
        val lastMessage = listElement.messages.last()
        val pp = listElement.user.getProfilePicture()

        if(pp == null)
            holder.icon.setImageResource(R.mipmap.ic_launcher)
        else
            holder.icon.setImageDrawable(pp)

        holder.title.text = "${listElement.user.getFirstName()} ${listElement.user.getLastName()}"
        holder.desc.text = "${lastMessage.sender}: ${lastMessage.body}"

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
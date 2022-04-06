package com.github.brugapp.brug.view_model

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ConvResponse

/**
 * Custom adapter class for the RecyclerView lists in ChatMenuActivity
 */
class ConversationListAdapter(
    private val chatList: MutableList<ConvResponse>,
    private val onItemClicked: (ConvResponse) -> Unit
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

        //TODO: CHECK IF THERE ARE SOME CASES NOT HANDLED PROPERLY
        if(listElement.onError == null){
            val conversation = listElement.onSuccess!!
            if(conversation.userFieldsInfos.onError == null){

                holder.title.text = conversation.userFieldsInfos.onSuccess!!.first
                if(conversation.userFieldsInfos.onSuccess!!.second.onError == null){
                    holder.icon.setImageURI(Uri.parse(conversation.userFieldsInfos.onSuccess!!.second.onSuccess!!.path))
                }

                val lastMessageContent = if(conversation.messages.isEmpty()){
                    ""
                } else {
                    val lastMessage = conversation.messages.last()
                    if(lastMessage.onError != null){
                        "Unable to retrieve the last message correctly"
                    } else {
                        lastMessage.onSuccess!!.body
                    }
                }
                holder.desc.text = lastMessageContent
            }
        }
    }

    // Returns the number of elements in the list
    override fun getItemCount(): Int {
        return chatList.size
    }

}
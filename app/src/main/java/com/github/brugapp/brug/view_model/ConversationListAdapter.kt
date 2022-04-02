package com.github.brugapp.brug.view_model

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ConvResponse
import com.github.brugapp.brug.data.MessageResponse

/**
 * Custom adapter class for the RecyclerView lists in ChatMenuActivity
 */
class ConversationListAdapter(
    private val chatList: MutableList<ConvResponse>,
    private val onItemClicked: (ConvResponse) -> Unit //TODO: CHECK IF CORRECT
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

        //TODO: NEED ICON, FULL NAME OF USER, AND LAST MESSAGE CONTENT
        val defaultIconPath = "/Users/mouniraki/Documents/SDP/BrugApp/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp"
        val userInfos: Pair<String, String>
        val lastMessageContent: String // = ""

        if(listElement.onError != null){
            Log.e("Firebase error", listElement.onError.toString())
        } else {
            val conversation = listElement.onSuccess!!

            // FOR THE (FULL_USERNAME, USER_ICON_PATH) PAIR
            userInfos = if(conversation.userInfos.onError != null){
                Log.e("Firebase error", conversation.userInfos.onError.toString())
                Pair("User not found", defaultIconPath)
            } else {
                conversation.userInfos.onSuccess!!
            }

            // FOR THE LAST MESSAGE CONTENT
            lastMessageContent =
                if(conversation.messages.isEmpty()){
                    ""
                } else {
                    val lastMessage: MessageResponse = conversation.messages.last()
                    if(lastMessage.onError != null){
                        Log.e("Firebase error", lastMessage.onError.toString())
                        "Unable to retrieve last message"
                    } else {
                        lastMessage.onSuccess!!.body
                    }
                }

            holder.title.text = userInfos.first
            holder.icon.setImageURI(Uri.parse(userInfos.second))
            holder.desc.text = lastMessageContent
        }

//        // TODO: replace the hardcoded image ID by the ID of the profile-pic of user
//        val lastMessage = listElement.messages.last()
//        holder.icon.setImageResource(R.mipmap.ic_launcher)
//        holder.title.text = "${listElement.user.getFirstName()} ${listElement.user.getLastName()}"
//        holder.desc.text = "${lastMessage.sender}: ${lastMessage.body}"

    }

    // Returns the number of elements in the list
    override fun getItemCount(): Int {
        return chatList.size
    }

}
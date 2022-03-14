package com.github.brugapp.brug

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

private const val MAX_DESC_LENGTH = 50

class ChatCustomAdapter(private val list: List<ChatViewModel>) :
    RecyclerView.Adapter<ChatCustomAdapter.ViewHolder>() {

    // Creates new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_entry_layout, parent, false)
        return ViewHolder(view)
    }

    // Binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val chatViewModel = list[position]

        holder.icon.setImageResource(chatViewModel.profilePicId)
        holder.title.text = chatViewModel.name
        holder.desc.text = chatViewModel.lastMessage
    }

    // Returns the number of elements in the list
    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(chatView: View) : RecyclerView.ViewHolder(chatView){
        val icon: ImageView = chatView.findViewById(R.id.chat_entry_profilepic)
        val title: TextView = chatView.findViewById(R.id.chat_entry_title)
        val desc: TextView = chatView.findViewById(R.id.chat_entry_desc)
    }

}
package com.github.brugapp.brug

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemsCustomAdapter(private val list: List<ItemsViewModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Creates new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_layout, parent, false)
        return ViewHolder(view)
    }

    // Binds the list items to a view
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val itemsViewModel = list[position]

        holder.icon.setImageResource(itemsViewModel.image)
        holder.title.text = itemsViewModel.title
        holder.desc.text = itemsViewModel.description
    }

    // Returns the number of elements in the list
    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val icon: ImageView = itemView.findViewById(R.id.list_item_icon)
        val title: TextView = itemView.findViewById(R.id.list_item_title)
        val desc: TextView = itemView.findViewById(R.id.list_item_desc)
    }


}
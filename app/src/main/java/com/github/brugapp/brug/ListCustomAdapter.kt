package com.github.brugapp.brug

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListCustomAdapter(
    private val list: List<ListViewModel>,
    private val layout: Int,
    private val iconId: Int,
    private val titleId: Int,
    private val descId: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Creates new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return ViewHolder(view, iconId, titleId, descId)
    }

    // Binds the list items to a view
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val listElement = list[position]
        holder.icon.setImageResource(listElement.iconId)
        holder.title.text = listElement.title
        holder.desc.text = listElement.desc
    }

    // Returns the number of elements in the list
    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(
        view: View,
        iconId: Int,
        titleId: Int,
        descId: Int
    ) : RecyclerView.ViewHolder(view){
        val icon: ImageView = view.findViewById(iconId)
        val title: TextView = view.findViewById(titleId)
        val desc: TextView = view.findViewById(descId)
    }


}
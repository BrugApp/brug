package com.github.brugapp.brug.view_model

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListViewHolder (view: View,
                      iconId: Int,
                      titleId: Int,
                      descId: Int,
                      onItemClicked: (Int) -> Unit
) : RecyclerView.ViewHolder(view){

        init{
            itemView.setOnClickListener {
                onItemClicked(adapterPosition)
            }
        }

        val icon: ImageView = view.findViewById(iconId)
        val title: TextView = view.findViewById(titleId)
        val desc: TextView = view.findViewById(descId)
}
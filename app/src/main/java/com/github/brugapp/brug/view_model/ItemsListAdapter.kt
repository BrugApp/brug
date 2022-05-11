package com.github.brugapp.brug.view_model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.MyItem

/**
 * Custom adapter class for the RecyclerView lists in ItemsMenuActivity
 */
class ItemsListAdapter(
    private val itemsList: MutableList<MyItem>,
    private val onItemClicked: (MyItem) -> Unit
) : RecyclerView.Adapter<ListViewHolder>() {

    // Creates new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_layout, parent, false)
        return ListViewHolder(
            view,
            R.id.list_item_icon,
            R.id.list_item_title,
            R.id.list_item_desc
        ) {
            onItemClicked(itemsList[it])
        }
    }

    // Binds the list items to a view
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listElement = itemsList[position]
        holder.icon.setImageResource(listElement.getRelatedIcon())
        holder.title.text = listElement.itemName
        holder.desc.text = listElement.itemDesc

    }

    // Returns the number of elements in the list
    override fun getItemCount(): Int {
        return itemsList.size
    }

}
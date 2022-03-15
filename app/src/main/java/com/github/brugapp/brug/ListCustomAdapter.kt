package com.github.brugapp.brug

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Custom adapter class for the RecyclerView lists in ChatMenuActivity
 * and ItemsMenuActivity.
 */
class ListCustomAdapter(
    private val layoutId: Int,
    private val iconId: Int,
    private val titleId: Int,
    private val descId: Int,
    private val onItemClicked: (ListViewModel) -> Unit
) : RecyclerView.Adapter<ListViewHolder>() {

    private val data = arrayListOf<ListViewModel>()

    // Creates new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
        return ListViewHolder(view, iconId, titleId, descId) {
            onItemClicked(data[it])
        }
    }

    // Binds the list items to a view
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listElement = data[position]
        holder.icon.setImageResource(listElement.iconId)
        holder.title.text = listElement.title
        holder.desc.text = listElement.desc
    }

    // Returns the number of elements in the list
    override fun getItemCount(): Int {
        return data.size
    }

    // Functions to handle entries in the data list
    fun addEntry(elem: ListViewModel): Boolean {
        return data.add(elem)
    }

    fun addEntryAtPos(position: Int, elem: ListViewModel) {
        return data.add(position, elem)
    }

    fun removeEntry(position: Int): ListViewModel {
        return data.removeAt(position)
    }

    fun getList(): ArrayList<ListViewModel> {
        return data
    }

}
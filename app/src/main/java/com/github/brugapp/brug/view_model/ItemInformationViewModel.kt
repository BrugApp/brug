package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.Item

class ItemInformationViewModel: ViewModel() {

    private lateinit var item:Item

    fun getText(item: Item): HashMap<String, String> {
        this.item = item
        val hash: HashMap<String,String> = HashMap()
        hash["title"] = item.getName()
        hash["image"] = item.getIcon().toString()
        hash["description"] = item.getDescription()
        hash["isLost"] = item.isLost().toString()
        return hash
    }

    fun setLostValue(checked: Boolean) {
       item.setLost(checked)
    }
}
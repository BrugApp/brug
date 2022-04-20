package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.MyItem

class ItemInformationViewModel: ViewModel() {

    private lateinit var item: MyItem

    fun getText(item: MyItem): HashMap<String, String> {
        this.item = item
        val hash: HashMap<String,String> = HashMap()
        hash["title"] = item.getItemName()
        hash["image"] = item.getRelatedIcon().toString()
        hash["description"] = item.getItemDesc()
        hash["isLost"] = item.isLost().toString()
        return hash
    }

    fun setLostValue(checked: Boolean) {
       item.changeLostStatus(checked)
    }
}
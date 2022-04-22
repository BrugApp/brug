package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.MyItem

class ItemInformationViewModel: ViewModel() {

    private lateinit var item: MyItem

    fun getText(item: MyItem): HashMap<String, String> {
        this.item = item
        val hash: HashMap<String,String> = HashMap()
        hash["title"] = item.itemName
        hash["image"] = item.getRelatedIcon().toString()
        hash["description"] = item.itemDesc
        hash["isLost"] = item.isLost().toString()
        return hash
    }

}
package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.MyItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ItemInformationViewModel: ViewModel() {

    private lateinit var qrId:String
    private lateinit var item: MyItem

    fun getText(item: MyItem): HashMap<String, String> {
        this.item = item
        qrId = Firebase.auth.uid +":"+ item.getItemID()
        val hash: HashMap<String,String> = HashMap()
        hash["title"] = item.itemName
        hash["image"] = item.getRelatedIcon().toString()
        hash["description"] = item.itemDesc
        hash["isLost"] = item.isLost().toString()
        return hash
    }
    fun getQrId(): String {
        return qrId
    }
}
package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.Item
import com.google.firebase.auth.FirebaseAuth

class ItemInformationViewModel : ViewModel() {

    private lateinit var qrId: String
    private lateinit var item: Item

    fun getText(item: Item, firebaseAuth: FirebaseAuth): HashMap<String, String> {
        this.item = item
        qrId = firebaseAuth.uid + ":" + item.getItemID()
        val hash: HashMap<String, String> = HashMap()
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
package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.fake.MockDatabase
import com.github.brugapp.brug.model.Item

class ItemInformationViewModel: ViewModel() {

    private lateinit var item:Item
    private lateinit var qrCode:String

    fun getText(item: Item): HashMap<String, String> {
        this.item = item
        //qrCode = FirebaseHelper.getCurrentUser("7IsGzvjHKd0KeeKK722m")!!.getId() + item.getId()
        qrCode = MockDatabase.currentUser.getId() +":"+ item.getId()
        val hash: HashMap<String,String> = HashMap()
        hash["title"] = item.getName()
        hash["image"] = item.getIcon().toString()
        hash["description"] = item.getDescription()
        hash["isLost"] = item.isLost().toString()
        return hash
    }

    //return concatenation of user id and item id
    fun getQrId(): String {
        return qrCode
    }


    fun setLostValue(checked: Boolean) {
       item.setLost(checked)
    }
}
package com.github.brugapp.brug.model

import android.graphics.drawable.Drawable
import java.io.Serializable

class MyUser(
    val uid: String,
    val firstName: String,
    val lastName: String,
    private var userIcon: Drawable?): Serializable {

    // DECLARED HERE TO AVOID HAVING TO INITIALIZE THE LISTS FOR USERS IN CONVERSATIONS
    private lateinit var itemsList: MutableList<MyItem>
    private lateinit var convList: MutableList<Conversation>

    /* MINIMALUSER FIELDS */
    fun getFullName(): String {
        return "$firstName $lastName"
    }

    fun getUserIcon(): Drawable? {
        return this.userIcon
    }

    fun setUserIcon(newIcon: Drawable?) {
        this.userIcon = newIcon
    }


    /* ITEMS LIST */
    fun initItemsList(list: List<MyItem>?){
        this.itemsList = mutableListOf()
        if (list != null) {
            this.itemsList.addAll(list)
        }
    }

    fun addItemToList(item: MyItem): Boolean{
        return this.itemsList.add(item)
    }

    fun deleteItemAtPos(position: Int): MyItem {
        return this.itemsList.removeAt(position)
    }


    /* CONVERSATION LIST */
    fun initConvList(list: List<Conversation>?){
        this.convList = mutableListOf()
        if(list != null){
            this.convList.addAll(list)
        }
    }

    fun addConvToList(conv: Conversation): Boolean {
        return this.convList.add(conv)
    }

    fun deleteConvAtPos(position: Int): Conversation {
        return this.convList.removeAt(position)
    }
}
package com.github.brugapp.brug.model

import com.github.brugapp.brug.R
import java.io.Serializable

class MyItem(val itemID: String,
             private var itemName: String,
             private var itemTypeID: Int,
             private var itemDesc: String,
             private var isLost: Boolean): Serializable {

    /* ITEM NAME */
    fun getItemName(): String {
        return this.itemName
    }

    fun setItemName(newName: String) {
        this.itemName = newName
    }


    /* ITEM TYPE */
    fun getRelatedItemType(): ItemType {
        return if(0 <= itemTypeID && itemTypeID < ItemType.values().size)
            ItemType.values()[itemTypeID]
        else ItemType.Other
    }

    fun getItemTypeID(): Int {
        return this.itemTypeID
    }

    fun setItemTypeID(typeID: Int) {
        this.itemTypeID = typeID
    }

    fun getRelatedIcon(): Int {
        return when(getRelatedItemType()){
            ItemType.Wallet -> R.drawable.ic_baseline_account_balance_wallet_24
            ItemType.Keys -> R.drawable.ic_baseline_vpn_key_24
            ItemType.CarKeys -> R.drawable.ic_baseline_car_rental_24
            ItemType.Phone -> R.drawable.ic_baseline_smartphone_24
            ItemType.Other -> R.drawable.ic_baseline_add_24
        }
    }

    /* ITEM DESC */
    fun getItemDesc(): String {
        return this.itemDesc
    }

    fun setItemDesc(newDesc: String) {
        this.itemDesc = newDesc
    }


    /* ITEM STATE */
    fun isLost(): Boolean{
        return this.isLost
    }

    fun changeLostStatus() {
        this.isLost = !this.isLost
    }

    override fun equals(other: Any?): Boolean {
        val otherItem = other as MyItem
        return this.itemID == otherItem.itemID
                && this.itemName == otherItem.itemName
                && this.itemTypeID == otherItem.itemTypeID
                && this.itemDesc == otherItem.itemDesc
                && this.isLost == otherItem.isLost
    }

}
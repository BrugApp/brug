package com.github.brugapp.brug.model

import com.github.brugapp.brug.R
import java.io.Serializable

class MyItem(
    val itemName: String,
    val itemTypeID: Int,
    val itemDesc: String,
    private var isLost: Boolean
) : Serializable {

    /* ITEM ID */
    private var itemID: String = ""

    fun setItemID(itemID: String) {
        this.itemID = itemID
    }

    fun getItemID(): String {
        return this.itemID
    }

    /* ITEM TYPE */
    fun getRelatedIcon(): Int {
        return when (getRelatedItemType()) {
            ItemType.Wallet -> R.drawable.ic_baseline_account_balance_wallet_24
            ItemType.Keys -> R.drawable.ic_baseline_vpn_key_24
            ItemType.CarKeys -> R.drawable.ic_baseline_car_rental_24
            ItemType.Phone -> R.drawable.ic_baseline_smartphone_24
            ItemType.Other -> R.drawable.ic_baseline_add_24
        }
    }

    private fun getRelatedItemType(): ItemType {
        return if (0 <= itemTypeID && itemTypeID < ItemType.values().size)
            ItemType.values()[itemTypeID]
        else ItemType.Other
    }

    /* ITEM STATE */
    fun isLost(): Boolean {
        return this.isLost
    }

    fun changeLostStatus(newStatus: Boolean) {
        this.isLost = newStatus
    }

    override fun equals(other: Any?): Boolean {
        val otherItem = other as MyItem
        return this.itemID == otherItem.itemID
                && this.itemName == otherItem.itemName
                && this.itemTypeID == otherItem.itemTypeID
                && this.itemDesc == otherItem.itemDesc
                && this.isLost == otherItem.isLost
    }

    override fun hashCode(): Int {
        var result = itemID.hashCode()
        result = 31 * result + itemName.hashCode()
        result = 31 * result + itemTypeID
        result = 31 * result + itemDesc.hashCode()
        result = 31 * result + isLost.hashCode()
        return result
    }

}
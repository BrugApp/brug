package com.github.brugapp.brug.model

import com.github.brugapp.brug.R
import java.io.Serializable
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MyItem(
    val itemName: String,
    val itemTypeID: Int,
    val itemDesc: String,
    private var isLost: Boolean
) : Serializable {

    /* ITEM ID */
    private var itemID: String = ""

    private var isDeleted: Boolean = false

    private val emptyDate = "EMPTY"

    private var deletedWhen:String = emptyDate


    fun getIsDeleted(): Boolean {
        return isDeleted
    }

    private val datePattern:String = "dd/MM/yyyy"

    fun setDeleted(deleted: Boolean) {
        //if(!isDeleted && deleted) setDeletedWhen()
        isDeleted = deleted
    }


    private fun setDeletedWhen() {
        val dateFormat = SimpleDateFormat(datePattern,Locale.US)
        deletedWhen = dateFormat.format(Date())
    }

    fun setDeletedWhen(date:String) {
        deletedWhen = date
    }

    fun isTooOld(): Boolean {
        return if(isDeleted) {
            val dateFormat = SimpleDateFormat(datePattern, Locale.US)
            val date = Date()
            //get 6 months ago
            val sixMonthsAgo = dateFormat.format(date.time - (1000L * 60L * 60L * 24L * 30L * 6L))
            deletedWhen < sixMonthsAgo
        }else {
            false
        }
    }


    fun getDeletedWhen(): String {
        if(deletedWhen.isEmpty()) return emptyDate
        return deletedWhen
    }


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
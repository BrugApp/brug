package com.github.brugapp.brug.model

import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.services.LocationService
import java.io.Serializable

class Item(
    val itemName: String,
    val itemTypeID: Int,
    val itemDesc: String,
    private var isFound: Boolean
) : Serializable {

    /* ITEM ID */
    private var itemID: String = ""
    private var lastLocation: LocationService? = null

    fun getLastLocation(): LocationService? {
        return lastLocation
    }
    fun setLastLocation(lon: Double, lat: Double){
        lastLocation = LocationService(lat, lon)
    }

    fun setItemID(itemID: String) {
        this.itemID = itemID
    }

    fun getItemID(): String {
        return this.itemID
    }

    /* ITEM TYPE */
    fun getRelatedIcon(): Int {
        return getRelatedItemType().getRelatedIcon()
    }

    private fun getRelatedItemType(): ItemType {
        return if (0 <= itemTypeID && itemTypeID < ItemType.values().size)
            ItemType.values()[itemTypeID]
        else ItemType.Other
    }

    /* ITEM STATE */
    fun isFound(): Boolean {
        return this.isFound
    }

    fun changeLostStatus(newStatus: Boolean) {
        this.isFound = newStatus
    }

    override fun equals(other: Any?): Boolean {
        val otherItem = other as Item
        return this.itemID == otherItem.itemID
                && this.itemName == otherItem.itemName
                && this.itemTypeID == otherItem.itemTypeID
                && this.itemDesc == otherItem.itemDesc
                && this.isFound == otherItem.isFound
    }

    override fun hashCode(): Int {
        var result = itemID.hashCode()
        result = 31 * result + itemName.hashCode()
        result = 31 * result + itemTypeID
        result = 31 * result + itemDesc.hashCode()
        result = 31 * result + isFound.hashCode()
        return result
    }

}

enum class ItemType {
    Wallet, Keys, CarKeys, Phone, Other;

    /* ITEM TYPE */
    fun getRelatedIcon(): Int {
        return when(this){
            Wallet -> R.drawable.ic_baseline_account_balance_wallet_24
            Keys -> R.drawable.ic_baseline_vpn_key_24
            CarKeys -> R.drawable.ic_baseline_car_rental_24
            Phone -> R.drawable.ic_baseline_smartphone_24
            Other -> R.drawable.ic_baseline_add_24
        }
    }
}
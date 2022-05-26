package com.github.brugapp.brug.model

import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.services.LocationService
import java.io.Serializable
import java.util.*

class Item(
    val itemName: String,
    private var itemTypeID: Int,
    val itemDesc: String,
    private var isLost: Boolean
) : Serializable {


    init {
        if (!(0 <= itemTypeID && itemTypeID < ItemType.values().size)) {
            this.itemTypeID = ItemType.Other.ordinal
        }
    }

    /* ITEM ID */
    private var itemID: String = ""
    private var lastLocation: LocationService? = null

    /**
     * returns the item's last location
     */
    fun getLastLocation(): LocationService? {
        return lastLocation
    }

    /**
     * set the item's last location
     *
     * @param lon longitude of the item
     * @param lat latitude of the item
     */
    fun setLastLocation(lon: Double, lat: Double){
        lastLocation = LocationService(lat, lon)
    }

    /**
     * sets the item's id
     *
     * @param itemID the item id
     */
    fun setItemID(itemID: String) {
        this.itemID = itemID
    }

    /**
     * returns the item's id
     */
    fun getItemID(): String {
        return this.itemID
    }

    /**
     * returns the item type's id
     */
    fun getItemTypeID(): Int {
        return this.itemTypeID
    }

    /**
     * returns the icon of the item
     */
    fun getRelatedIcon(): Int {
        return getRelatedItemType().getRelatedIcon()
    }

    private fun getRelatedItemType(): ItemType {
        return ItemType.values()[itemTypeID]
    }

    /**
     * return true if the item is lost
     */
    fun isLost(): Boolean {
        return this.isLost
    }

    /**
     * sets the lost status of the object to the new status
     *
     * @param newStatus the new lost status of the item
     */
    fun changeLostStatus(newStatus: Boolean) {
        this.isLost = newStatus
    }

    override fun equals(other: Any?): Boolean {
        val otherItem = other as Item
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

enum class ItemType {
    Wallet, Keys, CarKeys, Phone, Other;

    /**
     * returns the item's icon
     */
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
package com.github.brugapp.brug

import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.ItemType
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNot.not
import org.hamcrest.core.IsNull
import org.junit.Test

class ItemTest {
    @Test
    fun initItemWithoutIDCorrectlyInitializesItem() {
        val itemName = "Phone"
        val itemTypeID = ItemType.Phone.ordinal
        val itemDesc = "Samsung Galaxy S22"
        val isLost = false

        val item = Item(itemName, itemTypeID, itemDesc, isLost)

        assertThat(item.itemName, IsEqual(itemName))
        assertThat(item.getItemTypeID(), IsEqual(itemTypeID))
        assertThat(item.itemDesc, IsEqual(itemDesc))
        assertThat(item.isLost(), IsEqual(isLost))
        assertThat(item.getItemID(), IsEqual(""))
        assertThat(item.getLastLocation(), IsNull())
    }

    @Test
    fun initItemWithIDCorrectlyInitializesItem() {
        val itemID = "DUMMYID"
        val itemName = "Phone"
        val itemTypeID = ItemType.Phone.ordinal
        val itemDesc = "Samsung Galaxy S22"
        val isLost = false

        val item = Item(itemName, itemTypeID, itemDesc, isLost)
        item.setItemID(itemID)

        assertThat(item.itemName, IsEqual(itemName))
        assertThat(item.getItemTypeID(), IsEqual(itemTypeID))
        assertThat(item.itemDesc, IsEqual(itemDesc))
        assertThat(item.isLost(), IsEqual(isLost))
        assertThat(item.getItemID(), IsEqual(itemID))
    }

    @Test
    fun typeNameAndIconMatchPhoneTypeEntry() {
        val typeName = "Phone"
        val typeIcon = R.drawable.ic_baseline_smartphone_24

        val item = Item("Phone", ItemType.Phone.ordinal, "DUMMYDESC", false)

        assertThat(item.getItemTypeID(), IsEqual(ItemType.Phone.ordinal))
        assertThat(ItemType.values()[item.getItemTypeID()].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

    @Test
    fun typeNameAndIconMatchKeysTypeEntry() {
        val typeName = "Keys"
        val typeIcon = R.drawable.ic_baseline_vpn_key_24

        val item = Item("Keys", ItemType.Keys.ordinal, "DUMMYDESC", false)

        assertThat(item.getItemTypeID(), IsEqual(ItemType.Keys.ordinal))
        assertThat(ItemType.values()[item.getItemTypeID()].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

    @Test
    fun typeNameAndIconMatchCarKeysTypeEntry() {
        val typeName = "CarKeys"
        val typeIcon = R.drawable.ic_baseline_car_rental_24

        val item = Item("Car Keys", ItemType.CarKeys.ordinal, "DUMMYDESC", false)

        assertThat(item.getItemTypeID(), IsEqual(ItemType.CarKeys.ordinal))
        assertThat(ItemType.values()[item.getItemTypeID()].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

    @Test
    fun typeNameAndIconMatchWalletTypeEntry() {
        val typeName = "Wallet"
        val typeIcon = R.drawable.ic_baseline_account_balance_wallet_24

        val item = Item("Wallet", ItemType.Wallet.ordinal, "DUMMYDESC", false)

        assertThat(item.getItemTypeID(), IsEqual(ItemType.Wallet.ordinal))
        assertThat(ItemType.values()[item.getItemTypeID()].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

    @Test
    fun typeNameAndIconMatchOtherTypeEntry() {
        val typeName = "Other"
        val typeIcon = R.drawable.ic_baseline_add_24

        val item = Item("Other", ItemType.Other.ordinal, "DUMMYDESC", false)

        assertThat(item.getItemTypeID(), IsEqual(ItemType.Other.ordinal))
        assertThat(ItemType.values()[item.getItemTypeID()].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

    @Test
    fun compareIdenticalItemsReturnsEquality() {
        val item1 = Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)
        val item2 = Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)
        assertThat(item1, IsEqual(item2))
    }

    @Test
    fun compareAlmostItemsReturnsFalse() {
        val item1 = Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)
        val item2 = Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", true)
        assertThat(item1, IsNot(IsEqual(item2)))
    }

    @Test
    fun settingAndGettingLastLocationWorks() {
        val item = Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)
        val lon = 1.0
        val lat = 2.0
        item.setLastLocation(1.0, 2.0)
        val location = item.getLastLocation()
        assertThat(location, not(IsNull()))
        assertThat(location!!.getLatitude(), IsEqual(lat))
        assertThat(location.getLongitude(), IsEqual(lon))
    }

    @Test
    fun hashCodeReturnsCorrectHash() {
        val item = Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)
        var result = "".hashCode()
        result = 31 * result + "Phone".hashCode()
        result = 31 * result + ItemType.Phone.ordinal
        result = 31 * result + "Samsung Galaxy S22".hashCode()
        result = 31 * result + false.hashCode()
        assertThat(item.hashCode(), IsEqual(result))
    }

    @Test
    fun typeNameAndIconMatchOtherTypeEntryForWrongItemTypeId() {
        val typeName = "Other"
        val typeIcon = R.drawable.ic_baseline_add_24

        val item = Item("Other", ItemType.values().size, "DUMMYDESC", false)

        assertThat(item.getItemTypeID(), IsEqual(ItemType.Other.ordinal))
        assertThat(ItemType.values()[item.getItemTypeID()].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

}
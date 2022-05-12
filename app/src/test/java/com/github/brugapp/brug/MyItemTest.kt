package com.github.brugapp.brug

import android.util.Log
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.MyItem
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.junit.Test

class MyItemTest {
    @Test
    fun initItemWithoutIDCorrectlyInitializesItem() {
        val itemName = "Phone"
        val itemTypeID = ItemType.Phone.ordinal
        val itemDesc = "Samsung Galaxy S22"
        val isLost = false

        val item = MyItem(itemName, itemTypeID, itemDesc, isLost)

        assertThat(item.itemName, IsEqual(itemName))
        assertThat(item.itemTypeID, IsEqual(itemTypeID))
        assertThat(item.itemDesc, IsEqual(itemDesc))
        assertThat(item.isLost(), IsEqual(isLost))
        assertThat(item.getItemID(), IsEqual(""))
    }

    @Test
    fun initItemWithIDCorrectlyInitializesItem() {
        val itemID = "DUMMYID"
        val itemName = "Phone"
        val itemTypeID = ItemType.Phone.ordinal
        val itemDesc = "Samsung Galaxy S22"
        val isLost = false

        val item = MyItem(itemName, itemTypeID, itemDesc, isLost)
        item.setItemID(itemID)

        assertThat(item.itemName, IsEqual(itemName))
        assertThat(item.itemTypeID, IsEqual(itemTypeID))
        assertThat(item.itemDesc, IsEqual(itemDesc))
        assertThat(item.isLost(), IsEqual(isLost))
        assertThat(item.getItemID(), IsEqual(itemID))
    }

    @Test
    fun typeNameAndIconMatchPhoneTypeEntry() {
        val typeName = "Phone"
        val typeIcon = R.drawable.ic_baseline_smartphone_24

        val item = MyItem("Phone", ItemType.Phone.ordinal, "DUMMYDESC", false)

        assertThat(item.itemTypeID, IsEqual(ItemType.Phone.ordinal))
        assertThat(ItemType.values()[item.itemTypeID].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

    @Test
    fun typeNameAndIconMatchKeysTypeEntry() {
        val typeName = "Keys"
        val typeIcon = R.drawable.ic_baseline_vpn_key_24

        val item = MyItem("Keys", ItemType.Keys.ordinal, "DUMMYDESC", false)

        assertThat(item.itemTypeID, IsEqual(ItemType.Keys.ordinal))
        assertThat(ItemType.values()[item.itemTypeID].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

    @Test
    fun typeNameAndIconMatchCarKeysTypeEntry() {
        val typeName = "CarKeys"
        val typeIcon = R.drawable.ic_baseline_car_rental_24

        val item = MyItem("Car Keys", ItemType.CarKeys.ordinal, "DUMMYDESC", false)

        assertThat(item.itemTypeID, IsEqual(ItemType.CarKeys.ordinal))
        assertThat(ItemType.values()[item.itemTypeID].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

    @Test
    fun typeNameAndIconMatchWalletTypeEntry() {
        val typeName = "Wallet"
        val typeIcon = R.drawable.ic_baseline_account_balance_wallet_24

        val item = MyItem("Wallet", ItemType.Wallet.ordinal, "DUMMYDESC", false)

        assertThat(item.itemTypeID, IsEqual(ItemType.Wallet.ordinal))
        assertThat(ItemType.values()[item.itemTypeID].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

    @Test
    fun typeNameAndIconMatchOtherTypeEntry() {
        val typeName = "Other"
        val typeIcon = R.drawable.ic_baseline_add_24

        val item = MyItem("Other", ItemType.Other.ordinal, "DUMMYDESC", false)

        assertThat(item.itemTypeID, IsEqual(ItemType.Other.ordinal))
        assertThat(ItemType.values()[item.itemTypeID].toString(), IsEqual(typeName))
        assertThat(item.getRelatedIcon(), IsEqual(typeIcon))
    }

    @Test
    fun compareIdenticalItemsReturnsEquality() {
        val item1 = MyItem("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)
        val item2 = MyItem("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)
        assertThat(item1, IsEqual(item2))
    }

    @Test
    fun compareAlmostItemsReturnsFalse() {
        val item1 = MyItem("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)
        val item2 = MyItem("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", true)
        assertThat(item1, IsNot(IsEqual(item2)))
    }

    @Test
    fun itemCanBeDeleted() {
        val item = MyItem("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)
        item.setDeleted(true)
        assertThat(item.getIsDeleted(), IsEqual(true))
        val different = item.getDeletedWhen() != ""
        println( item.getDeletedWhen())
        assertThat(different,IsEqual(true))
    }

}
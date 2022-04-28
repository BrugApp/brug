package com.github.brugapp.brug

import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.model.MyItem
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val DUMMY_UID = "USER1"
private val DUMMY_ACCOUNT = BrugSignInAccount("Rayan", "Kikou", "", "")

private const val ITEM_ID = "DUMMYITEMID"
private var ITEM = MyItem("AirPods Pro Max", 0, "My Beloved AirPods", false)

class ItemRepoTest {
    //NEEDED SINCE @Before FUNCTIONS NEED TO BE VOID
    private fun addUserAndItem() = runBlocking {
        UserRepository.addUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT)
        ItemsRepository.addItemToUser(ITEM, DUMMY_UID)
    }

    private fun wipeAllItems() = runBlocking {
        ItemsRepository.deleteAllUserItems(DUMMY_UID)
    }

    @Before
    fun setUp() {
        addUserAndItem()
    }

    @After
    fun cleanUp() {
        wipeAllItems()
    }

    @Test
    fun addItemWithIDToWrongUserReturnsError() = runBlocking {
        assertThat(ItemsRepository.addItemWithItemID(ITEM, ITEM_ID, "WRONGUID").onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun addItemToWrongUserReturnsError() = runBlocking {
        assertThat(ItemsRepository.addItemToUser(ITEM, "WRONGUID").onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun addItemReturnsSuccessfully() = runBlocking {
        val response = ItemsRepository.addItemToUser(ITEM, DUMMY_UID)
        val list = ItemsRepository.getUserItemsFromUID(DUMMY_UID)

        assertThat(response.onSuccess, IsEqual(true))
        assertThat(list.isNullOrEmpty(), IsEqual(false))
//        assertThat(ItemsRepo.getUserItemsFromUID(USER.uid)!!.contains(ITEM), IsEqual(true))
    }

    @Test
    fun updateItemOfNonExistentUserReturnsError() = runBlocking {
        ItemsRepository.addItemWithItemID(ITEM, ITEM_ID, DUMMY_UID)
        val updatedItem = MyItem("AirPods 3", 1, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID(ITEM_ID)
        assertThat(ItemsRepository.updateItemFields(updatedItem, "WRONGUID").onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateNonExistentItemReturnsError() = runBlocking {
        ItemsRepository.addItemWithItemID(ITEM, ITEM_ID, DUMMY_UID)
        val updatedItem = MyItem("AirPods 3", 1, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID("WRONGITEMID")
        assertThat(ItemsRepository.updateItemFields(updatedItem, DUMMY_UID).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateItemReturnsSuccessfully() = runBlocking {
        ITEM.setItemID(ITEM_ID)
        ItemsRepository.addItemWithItemID(ITEM, ITEM_ID, DUMMY_UID)
        val updatedItem = MyItem("AirPods 3", 1, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID(ITEM_ID)
        assertThat(ItemsRepository.updateItemFields(updatedItem, DUMMY_UID).onSuccess, IsEqual(true))

        val items = ItemsRepository.getUserItemsFromUID(DUMMY_UID)
        assertThat(items.isNullOrEmpty(), IsEqual(false))
        assertThat(items!!.contains(updatedItem), IsEqual(true))
    }

    @Test
    fun deleteItemFromWrongUserReturnsError() = runBlocking {
        assertThat(ItemsRepository.deleteItemFromUser(ITEM_ID, "WRONGUID").onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteWrongItemReturnsError() = runBlocking {
        assertThat(ItemsRepository.deleteItemFromUser("WRONGITEMID", DUMMY_UID).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteItemReturnsSuccessfully() = runBlocking {
        ITEM.setItemID(ITEM_ID)
        ItemsRepository.addItemWithItemID(ITEM, ITEM_ID, DUMMY_UID)
        assertThat(ItemsRepository.deleteItemFromUser(ITEM.getItemID(), DUMMY_UID).onSuccess, IsEqual(true))
        val items = ItemsRepository.getUserItemsFromUID(DUMMY_UID)
        assertThat(items, IsNot(IsNull.nullValue()))
        assertThat(items!!.contains(ITEM), IsEqual(false))
    }

    @Test
    fun deleteAllItemsOfWrongUserReturnsError() = runBlocking {
        assertThat(ItemsRepository.deleteAllUserItems("WRONGUID").onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun getItemsFromWrongUserReturnsNull() = runBlocking {
        assertThat(ItemsRepository.getUserItemsFromUID("WRONGUID"), IsNull.nullValue())
    }

}
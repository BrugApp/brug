package com.github.brugapp.brug

import com.github.brugapp.brug.data.ItemsRepo
import com.github.brugapp.brug.data.UserRepo
import com.github.brugapp.brug.fake.FakeSignInAccount
import com.github.brugapp.brug.model.MyItem
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.Test

private const val DUMMY_UID = "USER1"
private val DUMMY_ACCOUNT = FakeSignInAccount("Rayan", "Kikou", "", "")

private const val ITEM_ID = "DUMMYITEMID"
private var ITEM = MyItem("AirPods Pro Max", 0, "My Beloved AirPods", false)

class ItemRepoTest {
    //NEEDED SINCE @Before FUNCTIONS NEED TO BE VOID
    private fun addUser() = runBlocking {
        UserRepo.addAuthUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT)
        ItemsRepo.addItemToUser(ITEM, DUMMY_UID)
    }

    @Before
    fun setUp() {
        addUser()
    }

    @Test
    fun addItemToWrongUserReturnsError() = runBlocking {
        assertThat(ItemsRepo.addItemToUser(ITEM, "WRONGUID").onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun addItemReturnsSuccessfully() = runBlocking {
        val response = ItemsRepo.addItemToUser(ITEM, DUMMY_UID)
        val list = ItemsRepo.getUserItemsFromUID(DUMMY_UID)

        assertThat(response.onSuccess, IsEqual(true))
        assertThat(list.isNullOrEmpty(), IsEqual(false))
//        assertThat(ItemsRepo.getUserItemsFromUID(USER.uid)!!.contains(ITEM), IsEqual(true))
    }

    @Test
    fun updateNonExistentItemReturnsError() = runBlocking {
        val updatedItem = MyItem("AirPods 3", 1, ITEM.itemDesc, ITEM.isLost())
        assertThat(ItemsRepo.updateItemFields(updatedItem, DUMMY_UID).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateItemReturnsSuccessfully() = runBlocking {
        ITEM.setItemID(ITEM_ID)
        ItemsRepo.addItemWithItemID(ITEM, ITEM_ID, DUMMY_UID)
        val updatedItem = MyItem("AirPods 3", 1, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID(ITEM_ID)
        assertThat(ItemsRepo.updateItemFields(updatedItem, DUMMY_UID).onSuccess, IsEqual(true))

        val items = ItemsRepo.getUserItemsFromUID(DUMMY_UID)
        assertThat(items.isNullOrEmpty(), IsEqual(false))
        assertThat(items!!.contains(updatedItem), IsEqual(true))
    }

    @Test
    fun deleteWrongItemReturnsError() = runBlocking {
        assertThat(ItemsRepo.deleteItemFromUser("WRONGITEMID", DUMMY_UID).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteItemReturnsSuccessfully() = runBlocking {
        ITEM.setItemID(ITEM_ID)
        ItemsRepo.addItemWithItemID(ITEM, ITEM_ID, DUMMY_UID)
        assertThat(ItemsRepo.deleteItemFromUser(ITEM.getItemID(), DUMMY_UID).onSuccess, IsEqual(true))
        val items = ItemsRepo.getUserItemsFromUID(DUMMY_UID)
        assertThat(items, IsNot(IsNull.nullValue()))
        assertThat(items!!.contains(ITEM), IsEqual(false))
    }

}
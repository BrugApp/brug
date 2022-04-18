package com.github.brugapp.brug

import com.github.brugapp.brug.data.ItemsRepo
import com.github.brugapp.brug.data.UserRepo
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.model.MyUser
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.Test

private val USER = MyUser("USER1", "Rayan", "Kikou", null)
private var ITEM = MyItem("DUMMYID", "AirPods Pro Max", 0, "My Beloved AirPods", false)

class ItemRepoTest {
    //NEEDED SINCE @Before FUNCTIONS NEED TO BE VOID
    private fun addUser() = runBlocking {
        UserRepo.addAuthUser(USER)
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
        val response = ItemsRepo.addItemToUser(ITEM, USER.uid)
        val list = ItemsRepo.getUserItemsFromUID(USER.uid)

        assertThat(response.onSuccess, IsEqual(true))
        assertThat(list.isNullOrEmpty(), IsEqual(false))
        assertThat(list!!.contains(ITEM), IsEqual(true))
//        assertThat(ItemsRepo.getUserItemsFromUID(USER.uid)!!.contains(ITEM), IsEqual(true))
    }

    @Test
    fun updateNonExistentItemReturnsError() = runBlocking {
        val updatedItem = MyItem("WRONGITEMID", "AirPods 3", 1, ITEM.getItemDesc(), ITEM.isLost())
        assertThat(ItemsRepo.updateItemFields(updatedItem, USER.uid).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateItemReturnsSuccessfully() = runBlocking {
        ItemsRepo.addItemToUser(ITEM, USER.uid)
        val updatedItem = MyItem(ITEM.itemID, "AirPods 3", 1, ITEM.getItemDesc(), ITEM.isLost())
        assertThat(ItemsRepo.updateItemFields(updatedItem, USER.uid).onSuccess, IsEqual(true))

        val items = ItemsRepo.getUserItemsFromUID(USER.uid)
        assertThat(items.isNullOrEmpty(), IsEqual(false))
        assertThat(items!!.contains(updatedItem), IsEqual(true))
    }

    @Test
    fun deleteWrongItemReturnsError() = runBlocking {
        assertThat(ItemsRepo.deleteItemFromUser("WRONGITEMID", USER.uid).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteItemReturnsSuccessfully() = runBlocking {
        ItemsRepo.addItemToUser(ITEM, USER.uid)
        assertThat(ItemsRepo.deleteItemFromUser(ITEM.itemID, USER.uid).onSuccess, IsEqual(true))
        val items = ItemsRepo.getUserItemsFromUID(USER.uid)
        assertThat(items, IsNot(IsNull.nullValue()))
        assertThat(items!!.contains(ITEM), IsEqual(false))
    }

}
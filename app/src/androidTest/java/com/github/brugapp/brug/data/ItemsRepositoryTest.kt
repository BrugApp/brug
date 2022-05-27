package com.github.brugapp.brug.data

import androidx.lifecycle.testing.TestLifecycleOwner
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
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
private var ITEM = Item("AirPods Pro Max", 0, "My Beloved AirPods", false)

class ItemsRepositoryTest {

    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()

    //NEEDED SINCE @Before FUNCTIONS NEED TO BE VOID
    private fun addUserAndItem() = runBlocking {
        UserRepository.addUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT, true, firestore)
        ITEM.setLastLocation(6.61,46.51)
        ItemsRepository.addItemToUser(ITEM, DUMMY_UID, firestore)
    }

    private fun wipeAllItems() = runBlocking {
        ItemsRepository.deleteAllUserItems(
            DUMMY_UID,
            firestore
        )
    }

    @Before
    fun setUp() {
        addUserAndItem()
        BrugDataCache.resetCachedItems()
    }

    @After
    fun cleanUp() {
        wipeAllItems()
    }

    @Test
    fun addItemWithIDToWrongUserReturnsError() = runBlocking {
        assertThat(ItemsRepository.addItemWithItemID(
            ITEM,
            ITEM_ID,
            "WRONGUID",
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun addItemToWrongUserReturnsError() = runBlocking {
        assertThat(ItemsRepository.addItemToUser(
            ITEM,
            "WRONGUID",
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun addItemReturnsSuccessfully() = runBlocking {
        val response = ItemsRepository.addItemToUser(
            ITEM,
            DUMMY_UID,
            firestore
        )

        ItemsRepository.getRealtimeUserItemsFromUID(
            DUMMY_UID,
            TestLifecycleOwner(),
            firestore
        )
        delay(2000)

        assertThat(response.onSuccess, IsEqual(true))
        assertThat(BrugDataCache.getCachedItems().value.isNullOrEmpty(), IsEqual(false))
    }

    @Test
    fun updateItemOfNonExistentUserReturnsError() = runBlocking {
        ItemsRepository.addItemWithItemID(
            ITEM,
            ITEM_ID,
            DUMMY_UID,
            firestore
        )
        val updatedItem = Item("AirPods 3", 1, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID(ITEM_ID)
        assertThat(ItemsRepository.updateItemFields(
            updatedItem,
            "WRONGUID",
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateNonExistentItemReturnsError() = runBlocking {
        ItemsRepository.addItemWithItemID(
            ITEM,
            ITEM_ID,
            DUMMY_UID,
            firestore
        )
        val updatedItem = Item("AirPods 3", 1, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID("WRONGITEMID")
        assertThat(ItemsRepository.updateItemFields(
            updatedItem,
            DUMMY_UID,
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateItemReturnsSuccessfully() = runBlocking {
        ITEM.setItemID(ITEM_ID)
        ItemsRepository.addItemWithItemID(
            ITEM,
            ITEM_ID,
            DUMMY_UID,
            firestore
        )
        val updatedItem = Item("AirPods 3", 1, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID(ITEM_ID)
        assertThat(ItemsRepository.updateItemFields(
            updatedItem,
            DUMMY_UID,
            firestore
        ).onSuccess, IsEqual(true))

        ItemsRepository.getRealtimeUserItemsFromUID(DUMMY_UID, TestLifecycleOwner(), firestore)
        delay(2000)

        assertThat(BrugDataCache.getCachedItems().value.isNullOrEmpty(), IsEqual(false))
        assertThat(BrugDataCache.getCachedItems().value!!.contains(updatedItem), IsEqual(true))
    }


    @Test
    fun setLocationToItemOfNonExistentUserReturnsError() = runBlocking {
        ItemsRepository.addItemWithItemID(
            ITEM,
            ITEM_ID,
            DUMMY_UID,
            firestore
        )
        val updatedItem = Item("AirPods Pro Max", 0, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID(ITEM_ID)
        assertThat(ItemsRepository.addLastLocation(
            "WRONG_UID",
            updatedItem.getItemID(),
            LocationService(10.2, 11.4),
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun setLocationToNonExistentItemReturnsError() = runBlocking {
        ItemsRepository.addItemWithItemID(
            ITEM,
            ITEM_ID,
            DUMMY_UID,
            firestore
        )
        val updatedItem = Item("AirPods Pro Max", 0, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID("WRONGITEMID")
        assertThat(ItemsRepository.addLastLocation(
            DUMMY_UID,
            updatedItem.getItemID(),
            LocationService(10.2, 11.4),
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun setLocationToExistingItemReturnsSuccessfully() = runBlocking {
        val location = LocationService(10.2, 11.4)
        ITEM.setItemID(ITEM_ID)
        ItemsRepository.addItemWithItemID(
            ITEM,
            ITEM_ID,
            DUMMY_UID,
            firestore
        )
        val updatedItem = Item("AirPods Pro Max", 0, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID(ITEM_ID)
        updatedItem.setLastLocation(location.getLongitude(), location.getLatitude())
        assertThat(ItemsRepository.addLastLocation(
            DUMMY_UID,
            ITEM_ID,
            location,
            firestore
        ).onSuccess, IsEqual(true))

        ItemsRepository.getRealtimeUserItemsFromUID(DUMMY_UID, TestLifecycleOwner(), firestore)
        delay(2000)

        assertThat(BrugDataCache.getCachedItems().value.isNullOrEmpty(), IsEqual(false))
        assertThat(BrugDataCache.getCachedItems().value!!.contains(updatedItem), IsEqual(true))
        val itemPosInList = BrugDataCache.getCachedItems().value!!.indexOf(updatedItem)
        assertThat(BrugDataCache.getCachedItems().value!!.get(itemPosInList).getLastLocation(), IsEqual(location))
    }

    @Test
    fun deleteItemFromWrongUserReturnsError() = runBlocking {
        assertThat(ItemsRepository.deleteItemFromUser(
            ITEM_ID,
            "WRONGUID",
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteWrongItemReturnsError() = runBlocking {
        assertThat(ItemsRepository.deleteItemFromUser(
            "WRONGITEMID",
            DUMMY_UID,
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteItemReturnsSuccessfully() = runBlocking {
        ITEM.setItemID(ITEM_ID)
        ItemsRepository.addItemWithItemID(
            ITEM,
            ITEM_ID,
            DUMMY_UID,
            firestore
        )
        assertThat(ItemsRepository.deleteItemFromUser(
            ITEM.getItemID(),
            DUMMY_UID,
            firestore
        ).onSuccess, IsEqual(true))

        ItemsRepository.getRealtimeUserItemsFromUID(DUMMY_UID, TestLifecycleOwner(), firestore)
        delay(2000)

        assertThat(BrugDataCache.getCachedItems().value, IsNot(IsEqual(mutableListOf())))
        assertThat(BrugDataCache.getCachedItems().value!!.contains(ITEM), IsEqual(false))
    }

    @Test
    fun deleteAllItemsOfWrongUserReturnsError() = runBlocking {
        assertThat(ItemsRepository.deleteAllUserItems(
            "WRONGUID",
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun getItemsFromWrongUserReturnsNull() = runBlocking {
        ItemsRepository.getRealtimeUserItemsFromUID("WRONGUID", TestLifecycleOwner(), firestore)
        delay(2000)
        assertThat(BrugDataCache.getCachedItems().value, IsEqual(mutableListOf()))
    }

    @Test
    fun getUserItemsFromUIDTest() = runBlocking {
        ITEM.setItemID(ITEM_ID)
        ITEM.setLastLocation(0.0, 0.0)
        val response = ItemsRepository.addItemWithItemID(ITEM, ITEM_ID, DUMMY_UID,firestore)
        ItemsRepository.getRealtimeUserItemsFromUID(DUMMY_UID, TestLifecycleOwner(), firestore)
        delay(2000)

        assertThat(response.onSuccess, IsEqual(true))
        assertThat(BrugDataCache.getCachedItems().value.isNullOrEmpty(), IsEqual(false))
        assertThat(BrugDataCache.getCachedItems().value!!.contains(ITEM), IsEqual(true))
    }

    @Test
    fun updateItemFieldsTest() = runBlocking {
        ITEM.setItemID(ITEM_ID)
        ITEM.setLastLocation(0.0, 0.0)
        ItemsRepository.addItemWithItemID(ITEM, ITEM_ID, DUMMY_UID,firestore)
        val updatedItem = Item("AirPods 3", 1, ITEM.itemDesc, ITEM.isLost())
        updatedItem.setItemID(ITEM_ID)
        assertThat(ItemsRepository.updateItemFields(updatedItem, DUMMY_UID,firestore).onSuccess, IsEqual(true))
        ItemsRepository.getRealtimeUserItemsFromUID(DUMMY_UID, TestLifecycleOwner(), firestore)
        delay(2000)

        assertThat(BrugDataCache.getCachedItems().value.isNullOrEmpty(), IsEqual(false))
        assertThat(BrugDataCache.getCachedItems().value!!.contains(updatedItem), IsEqual(true))
    }
}
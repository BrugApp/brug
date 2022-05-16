package com.github.brugapp.brug.map

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.ui.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


private const val APP_PACKAGE_NAME: String = "com.github.brugapp.brug"


private const val LIST_VIEW_ID: String = "$APP_PACKAGE_NAME:id/items_listview"
private const val LIST_ENTRY_ID: String = "$APP_PACKAGE_NAME:id/list_item_title"
private const val SNACKBAR_ID: String = "$APP_PACKAGE_NAME:id/snackbar_text"


private val ITEMS = listOf(
    MyItem("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false),
    MyItem("Wallet", ItemType.Wallet.ordinal, "With all my belongings", false),
    MyItem("Car Keys", ItemType.CarKeys.ordinal, "Lamborghini Aventador LP-780-4", false),
    MyItem("Keys", ItemType.Keys.ordinal, "Home keys", true)
)

private const val EPFL_LAT = 46.5197
private const val EPFL_LON = 6.5657

private var TEST_USER_UID = ""

@HiltAndroidTest
class MapBoxActivityTest {

    @get:Rule
    var rule = HiltAndroidRule(this)

    private val firebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firestore = FirebaseFakeHelper().providesFirestore()
    private  val TEST_EMAIL ="test@ItemsMenu.com"
    private  val TEST_PASSWORD = "123456"
    private val ACCOUNT1 = BrugSignInAccount("Rayan", "Kikou", "", "")
    companion object {
        var firstTime = true
    }


    private fun createTestUser(){
        runBlocking {
            if(firstTime){
                firebaseAuth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD).await()
                firstTime = false
            }
        }
    }
    private fun signInTestUser() {
        runBlocking {
            firebaseAuth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD).await()
            TEST_USER_UID = firebaseAuth.currentUser!!.uid
            UserRepository.addUserFromAccount(TEST_USER_UID, ACCOUNT1, true, firestore)
            var n_items = 0
            for(item in ITEMS){
                if (n_items == 0) {
                    item.setLastLocation(EPFL_LON, EPFL_LAT)
                    n_items = 1
                }
                ItemsRepository.addItemToUser(item, TEST_USER_UID, firestore)
            }
        }
    }

    private fun wipeAllItemsAndSignOut() {
        runBlocking {
            ItemsRepository.deleteAllUserItems(TEST_USER_UID, firestore)
        }
        firebaseAuth.signOut()
    }

    @Before
    fun setUp() {
        Intents.init()
        createTestUser()
        signInTestUser()
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ItemsMenuActivity::class.java).apply {
                putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_LAT)
                putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_LON)
        }
        ActivityScenario.launch<ItemsMenuActivity>(intent)
    }

    @After
    fun cleanUp() {
        Intents.release()
        wipeAllItemsAndSignOut()
    }

    @Test
    fun clickOnRandomPositionDoesNotOpenViewAnnotation() {

    }

    @Test
    fun clickOnItemOpensViewAnnotation() {

    }

    @Test
    fun clickOnWalkButtonOpensNavigation() {

    }

    @Test
    fun clickOnDriveButtonOpensNavigation() {

    }

    @Test
    fun viewAnnotationDisplaysNameOfItem() {

    }



}
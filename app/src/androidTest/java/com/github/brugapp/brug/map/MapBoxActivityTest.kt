package com.github.brugapp.brug.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.GrantPermissionRule
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.helpers.EspressoHelper
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.ui.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.Matchers.*
import org.junit.*


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

    @get:Rule
    val coarseLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    @get:Rule
    val fineLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private val firebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firestore = FirebaseFakeHelper().providesFirestore()
    private val TEST_EMAIL ="test@MapBox.com"
    private val TEST_PASSWORD = "123456"
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
    }

    @After
    fun cleanUp() {
        wipeAllItemsAndSignOut()
        Intents.release()
    }

    @Test
    fun clickOnRandomPositionDoesNotOpenViewAnnotation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_LAT)
            putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_LON)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(5000)
            EspressoHelper.clickIn(10,10)
            // if view annotation is not displayed, this will throw an exception
            Espresso.onView(withId(R.id.driveButton)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun clickOnItemOpensViewAnnotation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_LAT)
            putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_LON)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(5000)
            val mapViewMatcher = allOf(withId(R.id.mapView), ViewMatchers.hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(5000)

            Espresso.onView(withId(R.id.driveButton)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun clickOnWalkButtonOpensNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_LAT)
            putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_LON)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(5000)
            val mapViewMatcher = allOf(withId(R.id.mapView), hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(5000)

            Espresso.onView(withId(R.id.walkButton)).perform(click())
            Thread.sleep(5000)
            intended(IntentMatchers.hasComponent(NavigationToItemActivity::class.java.name))
        }
    }

    @Test
    fun clickOnDriveButtonOpensNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_LAT)
            putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_LON)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(5000)
            val mapViewMatcher = allOf(withId(R.id.mapView), hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(5000)
            Espresso.onView(withId(R.id.driveButton)).perform(click())
            Thread.sleep(5000)
            intended(IntentMatchers.hasComponent(NavigationToItemActivity::class.java.name))
        }
    }

    @Test
    fun viewAnnotationDisplaysNameOfItem() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_LAT)
            putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_LON)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(5000)
            val mapViewMatcher = allOf(withId(R.id.mapView), hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(5000)

            Espresso.onView(withId(R.id.itemNameOnMap)).check(matches(withText(ITEMS[0].itemName)))
        }
    }

}
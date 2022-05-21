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
import com.github.brugapp.brug.ITEMS_TEST_LIST_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.helpers.EspressoHelper
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.ui.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.Matchers.*
import org.junit.*

private val ITEMS = arrayListOf(
    Item("Wallet", ItemType.Wallet.ordinal, "With all my belongings", false),
    Item("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false),
    Item("Car Keys", ItemType.CarKeys.ordinal, "Lamborghini Aventador LP-780-4", false),
    Item("Keys", ItemType.Keys.ordinal, "Home keys", true)
)

private val EPFL_COORDINATES = Pair(46.5197, 6.5657)

//private val TEST_COORDINATES = listOf(
//    Pair(46.5197, 6.5657), // EPFL COORDINATES
//    Pair(47.3744489, 8.5410422), // ZURICH COORDINATES
//    Pair(46.2017559, 6.1466014), // GENEVA COORDINATES
//    Pair(48.8534951, 2.3483915) // PARIS COORDINATES
//)

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

    // TESTS DON'T WORK AS SOON AS THERE ARE 2 OR MORE ITEMS WITH A LAST LOCATION SET
//    private fun setLocationToItems() {
//        ITEMS.zip(TEST_COORDINATES).map { (item, coordinates) ->
//            item.setLastLocation(coordinates.second, coordinates.first)
//        }
//    }

    private fun setLocationToFirstItem(){
        ITEMS[0].setLastLocation(EPFL_COORDINATES.second, EPFL_COORDINATES.first)
    }

    @Before
    fun setUp() {
        Intents.init()
        setLocationToFirstItem()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    @Test
    fun clickOnRandomPositionDoesNotOpenViewAnnotation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            val epflCoordinates = EPFL_COORDINATES //TEST_COORDINATES[0]
            putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
            putExtra(EXTRA_DESTINATION_LATITUDE, epflCoordinates.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, epflCoordinates.second)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(10000)
            EspressoHelper.clickIn(10,10)
            Thread.sleep(10000)
            // if view annotation is not displayed, this will throw an exception
            Espresso.onView(withId(R.id.driveButton)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun clickOnItemOpensViewAnnotation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            val epflCoordinates = EPFL_COORDINATES //TEST_COORDINATES[0]
            putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
            putExtra(EXTRA_DESTINATION_LATITUDE, epflCoordinates.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, epflCoordinates.second)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(10000)
            val mapViewMatcher = allOf(withId(R.id.mapView), ViewMatchers.hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(10000)

            Espresso.onView(withId(R.id.driveButton)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun clickOnWalkButtonOpensNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            val epflCoordinates = EPFL_COORDINATES //TEST_COORDINATES[0]
            putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
            putExtra(EXTRA_DESTINATION_LATITUDE, epflCoordinates.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, epflCoordinates.second)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(10000)
            val mapViewMatcher = allOf(withId(R.id.mapView), hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(10000)

            Espresso.onView(withId(R.id.walkButton)).perform(click())
            Thread.sleep(10000)
            intended(IntentMatchers.hasComponent(NavigationToItemActivity::class.java.name))
        }
    }

    @Test
    fun clickOnDriveButtonOpensNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            val epflCoordinates = EPFL_COORDINATES //TEST_COORDINATES[0]
            putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
            putExtra(EXTRA_DESTINATION_LATITUDE, epflCoordinates.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, epflCoordinates.second)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(10000)
            val mapViewMatcher = allOf(withId(R.id.mapView), hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(10000)
            Espresso.onView(withId(R.id.driveButton)).perform(click())
            Thread.sleep(10000)
            intended(IntentMatchers.hasComponent(NavigationToItemActivity::class.java.name))
        }
    }

    @Test
    fun viewAnnotationDisplaysNameOfItem() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            val epflCoordinates = EPFL_COORDINATES //TEST_COORDINATES[0]
            putExtra(ITEMS_TEST_LIST_KEY, ITEMS)
            putExtra(EXTRA_DESTINATION_LATITUDE, epflCoordinates.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, epflCoordinates.second)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(10000)
            val mapViewMatcher = allOf(withId(R.id.mapView), hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(10000)

            Espresso.onView(withId(R.id.itemNameOnMap)).check(matches(withText(ITEMS[0].itemName)))
        }
    }

}
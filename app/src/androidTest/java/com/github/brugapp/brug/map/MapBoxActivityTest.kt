package com.github.brugapp.brug.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
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
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.helpers.EspressoHelper
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.ui.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.zoom
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private val TEST_ITEM = Item("Wallet", ItemType.Wallet.ordinal, "With all my belongings", false)
private val EPFL_COORDINATES = Pair(46.5197, 6.5657) // LAT, LON
private const val ZOOM_FACTOR = 22.0

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

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
        BrugDataCache.resetCachedItems()
    }

    private fun setTestList(): ArrayList<Item> {
        TEST_ITEM.setLastLocation(EPFL_COORDINATES.second, EPFL_COORDINATES.first)
        return arrayListOf(TEST_ITEM)
    }

    @Test
    fun clickOnRandomPositionDoesNotOpenViewAnnotation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            putExtra(ITEMS_TEST_LIST_KEY, setTestList())
            putExtra(EXTRA_MAP_ZOOM, ZOOM_FACTOR)
            putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_COORDINATES.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_COORDINATES.second)
        }
        ActivityScenario.launch<Activity>(intent).use{
            EspressoHelper.clickIn(10,10)
            Thread.sleep(3000)
            // if view annotation is not displayed, this will throw an exception
            Espresso.onView(withId(R.id.driveButton)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun clickOnItemOpensViewAnnotation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            putExtra(ITEMS_TEST_LIST_KEY, setTestList())
            putExtra(EXTRA_MAP_ZOOM, ZOOM_FACTOR)
            putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_COORDINATES.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_COORDINATES.second)
        }
        ActivityScenario.launch<Activity>(intent).use{
            val mapViewMatcher = allOf(withId(R.id.mapView), ViewMatchers.hasMinimumChildCount(1))
            Log.e("ZOOM VALUE", zoom().getLiteral<Int>().toString())
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(3000)

            Espresso.onView(withId(R.id.driveButton)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun clickOnWalkButtonOpensNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            putExtra(ITEMS_TEST_LIST_KEY, setTestList())
            putExtra(EXTRA_MAP_ZOOM, ZOOM_FACTOR)
            putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_COORDINATES.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_COORDINATES.second)
        }
        ActivityScenario.launch<Activity>(intent).use{
            val mapViewMatcher = allOf(withId(R.id.mapView), hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(3000)

            Espresso.onView(withId(R.id.walkButton)).perform(click())
            Thread.sleep(3000)
            intended(IntentMatchers.hasComponent(NavigationToItemActivity::class.java.name))
        }
    }

    @Test
    fun clickOnDriveButtonOpensNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            putExtra(ITEMS_TEST_LIST_KEY, setTestList())
            putExtra(EXTRA_MAP_ZOOM, ZOOM_FACTOR)
            putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_COORDINATES.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_COORDINATES.second)
        }
        ActivityScenario.launch<Activity>(intent).use{
            val mapViewMatcher = allOf(withId(R.id.mapView), hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(3000)

            Espresso.onView(withId(R.id.driveButton)).perform(click())
            Thread.sleep(3000)
            intended(IntentMatchers.hasComponent(NavigationToItemActivity::class.java.name))
        }
    }

    @Test
    fun viewAnnotationDisplaysNameOfItem() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MapBoxActivity::class.java).apply {
            putExtra(ITEMS_TEST_LIST_KEY, setTestList())
            putExtra(EXTRA_MAP_ZOOM, ZOOM_FACTOR)
            putExtra(EXTRA_DESTINATION_LATITUDE, EPFL_COORDINATES.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, EPFL_COORDINATES.second)
        }
        ActivityScenario.launch<Activity>(intent).use{
            val mapViewMatcher = allOf(withId(R.id.mapView), hasMinimumChildCount(1))
            Espresso.onView(mapViewMatcher).perform(EspressoHelper.clickInFraction(0.5,0.5))
            Thread.sleep(3000)

            Espresso.onView(withId(R.id.itemNameOnMap)).check(matches(withText(TEST_ITEM.itemName)))
        }
    }

}
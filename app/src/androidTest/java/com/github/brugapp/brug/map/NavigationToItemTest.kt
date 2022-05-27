package com.github.brugapp.brug.map

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.GrantPermissionRule
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.di.sign_in.module.NavigationOptionsModule
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.ui.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(NavigationOptionsModule::class)
class NavigationToItemTest {

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Module
    @InstallIn(ActivityComponent::class)
    object ReplayNavigationModule {
        @Provides
        fun providesNavigationOptions(@ActivityContext activity: Context): NavigationOptions {
            val activityNav = activity as NavigationToItemActivity
            val replayLocationEngine = ReplayLocationEngine(activityNav.mapboxReplayer)
            return NavigationOptions.Builder(activity.applicationContext)
                .accessToken(activity.getString(R.string.mapbox_access_token))
                .locationEngine(replayLocationEngine)
                .build()
        }
    }

    private var TEST_USER_UID = "TwSXfeusCKN95UvlGgY4uvEnXpl2"
    private val MICROSOFT_COORDINATES = Pair(37.41261747639361, -122.07098371482229) // LAT, LON

    @get:Rule
    val coarseLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    @get:Rule
    val fineLocationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()
    companion object{
        var firstTime = true
    }

    private fun createUser(){
        runBlocking{
            if(firstTime) {
                firebaseAuth.createUserWithEmailAndPassword("navigation@unlost.com", "123456").await()
                firstTime = false
            }
        }
    }
    private fun signInTestUser() {
        runBlocking {
            firebaseAuth.signInWithEmailAndPassword(
                "navigation@unlost.com",
                "123456").await()
        }
    }

    private fun wipeItemsAndSignOut() {
        runBlocking {
            ItemsRepository.deleteAllUserItems(
                TEST_USER_UID,
                firestore
            )
        }
        firebaseAuth.signOut()
    }

    @Before
    fun setUp() {
        Intents.init()
        createUser()
        signInTestUser()
        getUserUid()
        val intent = Intent(ApplicationProvider.getApplicationContext(), AddItemActivity::class.java)
        ActivityScenario.launch<AddItemActivity>(intent)
    }

    private fun getUserUid() {
        runBlocking {
            TEST_USER_UID = firebaseAuth.currentUser?.uid!!
        }
    }

    @After
    fun cleanUp() {
        Intents.release()
        wipeItemsAndSignOut()
    }

    @Test
    fun recenterItemDoesNotStartNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, NavigationToItemActivity::class.java).apply {
            putExtra(EXTRA_DESTINATION_LATITUDE, MICROSOFT_COORDINATES.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, MICROSOFT_COORDINATES.second)
            putExtra(EXTRA_NAVIGATION_MODE, DirectionsCriteria.PROFILE_DRIVING)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(10000)
            Espresso.onView(ViewMatchers.withId(R.id.recenter_nav)).perform(ViewActions.click())
            Thread.sleep(10000)
            Espresso.onView(ViewMatchers.withId(R.id.stop)).check(
                ViewAssertions.matches(
                    Matchers.not(ViewMatchers.isDisplayed())
                )
            )
        }
    }

    @Test
    fun startNavigationItemButtonStartsNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, NavigationToItemActivity::class.java).apply {
            putExtra(EXTRA_DESTINATION_LATITUDE, MICROSOFT_COORDINATES.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, MICROSOFT_COORDINATES.second)
            putExtra(EXTRA_NAVIGATION_MODE, DirectionsCriteria.PROFILE_DRIVING)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(10000)
            Espresso.onView(ViewMatchers.withId(R.id.start_navigation_button)).perform(ViewActions.click())
            Thread.sleep(10000)
            Espresso.onView(ViewMatchers.withId(R.id.soundButton)).perform(ViewActions.click())
            Thread.sleep(1000)
            Espresso.onView(ViewMatchers.withId(R.id.soundButton)).perform(ViewActions.click())
            Thread.sleep(1000)
            Espresso.onView(ViewMatchers.withId(R.id.routeOverview)).perform(ViewActions.click())
            Thread.sleep(1000)
            Espresso.onView(ViewMatchers.withId(R.id.recenter_nav)).perform(ViewActions.click())
            Thread.sleep(10000)
            Espresso.onView(ViewMatchers.withId(R.id.stop)).check(
                ViewAssertions.matches(ViewMatchers.isDisplayed())
            )
        }
    }

    @Test
    fun clickingOnStopStopsNavigationAndGoesBackToItemMapActivity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, NavigationToItemActivity::class.java).apply {
            putExtra(EXTRA_DESTINATION_LATITUDE, MICROSOFT_COORDINATES.first)
            putExtra(EXTRA_DESTINATION_LONGITUDE, MICROSOFT_COORDINATES.second)
            putExtra(EXTRA_NAVIGATION_MODE, DirectionsCriteria.PROFILE_DRIVING)
        }
        ActivityScenario.launch<Activity>(intent).use{
            Thread.sleep(10000)
            Espresso.onView(ViewMatchers.withId(R.id.start_navigation_button)).perform(ViewActions.click())
            Thread.sleep(5000)
            Espresso.onView(ViewMatchers.withId(R.id.stop)).perform(ViewActions.click())
            Thread.sleep(10000)
            intended(IntentMatchers.hasComponent(ItemMapActivity::class.java.name))
        }
    }


}
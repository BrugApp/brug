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
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import com.github.brugapp.brug.MESSAGE_TEST_LIST_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.ui.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MapBoxActivityNoLocationTest {

    @get:Rule
    var rule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule1: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    @get:Rule
    val permissionRule2: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    @Test
    fun mapboxDoesNotCrashWhenNoLocationIsProvided() {
        runBlocking {
            firebaseAuth.createUserWithEmailAndPassword("goa@efgh.com", "123456").await()
            firebaseAuth.signInWithEmailAndPassword("goa@efgh.com", "123456").await()
        }

        val context = ApplicationProvider.getApplicationContext<Context>()

        val intent = Intent(context, MapBoxActivity::class.java)

        ActivityScenario.launch<Activity>(intent).use{

            Intents.intended(
                Matchers.allOf(
                    IntentMatchers.toPackage("com.github.brugapp.brug")
                )
            )
        }
        firebaseAuth.signOut()
    }
}
package com.github.brugapp.brug

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.di.sign_in.module.ActivityResultModule
import com.github.brugapp.brug.fake.MockDatabase
import com.github.brugapp.brug.ui.SettingsActivity
import com.github.brugapp.brug.ui.SignInActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Settings Activity Tests
 *
 */
//@UninstallModules(
//    SettingsModule::class
//)
@HiltAndroidTest
@UninstallModules(ActivityResultModule::class)
@RunWith(AndroidJUnit4::class)
class SettingsTest {

    @Module
    @InstallIn(ActivityComponent::class)
    object FakeImageModule {
        @Provides
        fun provideFakeActivityRegistry(@ActivityContext activity: Context): ActivityResultRegistry {
            val resources: Resources = activity.resources
            val resId  = R.mipmap.ic_launcher
            val expectedResult = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(
                    resId
                ) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(
                    resId
                )
            )

            val testRegistry = object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?
                ) {
                    dispatchResult(requestCode, expectedResult)
                }
            }
            return testRegistry
        }
    }

    @get:Rule
    val rule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    @Test
    fun canSelectPicture(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), SettingsActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            Espresso.onView(ViewMatchers.withId(R.id.loadButton)).perform(click())
            Log.d("temp",MockDatabase.currentUser.getProfilePicture().toString())
            assert(MockDatabase.currentUser.getProfilePicture()==null )
        }
    }

}
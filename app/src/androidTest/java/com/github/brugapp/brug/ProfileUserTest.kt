package com.github.brugapp.brug

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.di.sign_in.module.ActivityResultModule
import com.github.brugapp.brug.fake.MockDatabase
import com.github.brugapp.brug.ui.ProfilePictureSetActivity
import com.github.brugapp.brug.ui.SignInActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Description
import org.hamcrest.Matchers.nullValue
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Settings Activity Tests
 *
 */
@HiltAndroidTest
@UninstallModules(ActivityResultModule::class)
@RunWith(AndroidJUnit4::class)
class ProfileUserTest {

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
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfilePictureSetActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            onView(withId(R.id.loadButton)).perform(click())
            assertThat(MockDatabase.currentUser.getProfilePicture(), Is(nullValue()) )
        }
    }

    @Test
    fun correctNameIsDisplayed(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfilePictureSetActivity::class.java)

        val name: String = MockDatabase.currentUser.getFirstName() + " " + MockDatabase.currentUser.getLastName()
        ActivityScenario.launch<SignInActivity>(intent).use {
            onView(withId(R.id.username)).check(matches(withText(name)))
        }
    }

    @Test
    fun initProfilePictureAndChange(){
        correctProfilePictureDisplayed()
        cleanUp()
        setUp()
        profilePictureCanBeChanged()
    }



    private fun correctProfilePictureDisplayed(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfilePictureSetActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            onView(withId(R.id.imgProfile)).check(matches(withDrawable(R.drawable.ic_person_outline_black_24dp)))
        }
    }

    private fun profilePictureCanBeChanged(){
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfilePictureSetActivity::class.java)
        val drawableRes = R.mipmap.ic_launcher
        val drawable  = ContextCompat.getDrawable(ApplicationProvider.getApplicationContext(), drawableRes)

        MockDatabase.currentUser.setProfilePicture(drawable)

        ActivityScenario.launch<SignInActivity>(intent).use {
            onView(withId(R.id.imgProfile)).check(matches(withDrawable(drawableRes)))
        }
    }

    private fun withDrawable(@DrawableRes id: Int) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("$id")
        }

        override fun matchesSafely(view: View): Boolean {
            val context = view.context
            val expectedBitmap = context.getDrawable(id)?.toBitmap()

            return view is ImageView && view.drawable.toBitmap().sameAs(expectedBitmap)
        }
    }

}
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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.module.ActivityResultModule
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.ui.ProfilePictureSetActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
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
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Settings Activity Tests
 *
 */

private const val TEST_USERNAME = "Test Unlost"

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


    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firebaseStorage: FirebaseStorage = FirebaseFakeHelper().providesStorage()

    private fun signInTestAccount(){
        runBlocking{
            firebaseAuth.signInWithEmailAndPassword(
                "test@unlost.com",
                "123456"
            ).await()
        }
    }

    private fun removeIconAndSignOut() {
        runBlocking {
            UserRepository
                .resetUserIcon(
                    firebaseAuth.currentUser!!.uid,firestore)
        }
        firebaseAuth.signOut()
    }


    @Before
    fun setUp() {
        Intents.init()
        signInTestAccount()
        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfilePictureSetActivity::class.java)
//        intent.putExtra(USER_INTENT_KEY, DUMMY_USER)
        ActivityScenario.launch<ProfilePictureSetActivity>(intent)
    }

    @After
    fun cleanUp() {
        Intents.release()
        removeIconAndSignOut()
    }

//    @Test
//    fun canSelectPicture(){
//        Thread.sleep(8000)
//        onView(withId(R.id.loadButton)).perform(click())
////        assertThat(DUMMY_USER.getUserIcon(), Is(nullValue()) )
//
//    }

    @Test
    fun correctNameIsDisplayed(){
        val name: String = TEST_USERNAME//MockDatabase.currentUser.getFirstName() + " " + MockDatabase.currentUser.getLastName()
        Thread.sleep(30000)
        onView(withId(R.id.username)).check(matches(withText(name)))
    }

    @Test
    fun initProfilePictureAndChange(){
        Thread.sleep(30000)
        correctProfilePictureDisplayed()
        cleanUp()
        setUp()
        profilePictureCanBeChanged()
    }


    private fun correctProfilePictureDisplayed(){
        Thread.sleep(30000)
        onView(withId(R.id.imgProfile)).check(matches(withDrawable(R.mipmap.ic_launcher_round)))
    }

    private fun profilePictureCanBeChanged(){
//        val intent = Intent(ApplicationProvider.getApplicationContext(), ProfilePictureSetActivity::class.java)
        val drawableRes = R.mipmap.ic_launcher
        val drawable  = ContextCompat.getDrawable(ApplicationProvider.getApplicationContext(), drawableRes)

//        DUMMY_USER.setUserIcon(drawable)
        val response = runBlocking { UserRepository.updateUserIcon(
            firebaseAuth.currentUser!!.uid, drawable!!,firebaseAuth,firebaseStorage,firestore) }
        assertThat(response.onSuccess, IsEqual(true))
//        MockDatabase.currentUser.setProfilePicture(drawable)

//        ActivityScenario.launch<SignInActivity>(intent).use {
        onView(withId(R.id.imgProfile)).check(matches(withDrawable(drawableRes)))
//        }
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
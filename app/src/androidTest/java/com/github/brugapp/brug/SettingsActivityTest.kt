package com.github.brugapp.brug

import android.R.attr.bitmap
import android.app.Activity
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
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
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.di.sign_in.module.ActivityResultModule
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.ui.SettingsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.internal.matchers.Null
import java.io.File
import java.io.FileOutputStream
import java.util.*


private const val TEST_USERNAME = "Rayan Kikou"

/**
 * Settings Activity Tests
 */
@HiltAndroidTest
@UninstallModules(ActivityResultModule::class)
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

    @Module
    @InstallIn(ActivityComponent::class)
    object FakeImageModule {
        @Provides
        fun provideFakeActivityRegistry(@ActivityContext activity: Context): ActivityResultRegistry {
            val resources: Resources = activity.resources
            val resId = R.mipmap.ic_launcher
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

    private var testUserUid: String = ""
    private val TEST_PASSWORD: String = "123456"
    private val TEST_EMAIL: String = "unlost@settingsActivity.com"
    private val ACCOUNT1 = BrugSignInAccount("Rayan", "Kikou", "", "")

    @get:Rule
    val rule = HiltAndroidRule(this)

    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firebaseStorage: FirebaseStorage = FirebaseFakeHelper().providesStorage()
    private lateinit var image:Bitmap

    companion object {
        var firstTime = true
    }

    private fun createTestUser() {
        runBlocking {
            if (firstTime) {
                firebaseAuth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD).await()
                firstTime = false
            }
        }
    }

    private fun signInTestAccount() {
        runBlocking {
            firebaseAuth.signInWithEmailAndPassword(
                TEST_EMAIL,
                TEST_PASSWORD
            ).await()
            testUserUid = firebaseAuth.currentUser!!.uid
            UserRepository
                .addUserFromAccount(testUserUid, ACCOUNT1, true, firestore)
        }
    }

    private fun removeIconAndSignOut() {
        runBlocking {
            UserRepository
                .resetUserIcon(
                    testUserUid, firestore
                )
        }
        firebaseAuth.signOut()
    }

    @Before
    fun setUp() {
        Intents.init()
        createTestUser()
        signInTestAccount()
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), SettingsActivity::class.java)
        ActivityScenario.launch<Activity>(intent)
    }

    @After
    fun cleanUp() {
        Intents.release()
        removeIconAndSignOut()
    }

    @Test
    fun correctNameIsDisplayed() {
        val name: String = TEST_USERNAME
        Thread.sleep(3000)
        onView(ViewMatchers.withId(R.id.settingsUserName))
            .check(ViewAssertions.matches(ViewMatchers.withText(name)))
    }

    @Test
    fun initProfilePictureAndChange(){
        Thread.sleep(3000)
        correctProfilePictureDisplayed()
        cleanUp()
        setUp()
        profilePictureCanBeChanged()
    }

    private fun correctProfilePictureDisplayed(){
        Thread.sleep(3000)
        onView(ViewMatchers.withId(R.id.settingsUserPic))
            .check(ViewAssertions.matches(withDrawable(R.mipmap.ic_launcher_round)))
    }


    private fun profilePictureCanBeChanged(){
        val drawableRes = R.mipmap.ic_launcher
        val drawable  = ContextCompat.getDrawable(ApplicationProvider.getApplicationContext(), drawableRes)

        val response = runBlocking { UserRepository.updateUserIcon(
            firebaseAuth.currentUser!!.uid, drawable!!,firebaseAuth,firebaseStorage,firestore) }
        ViewMatchers.assertThat(response.onSuccess, IsEqual(true))

        onView(ViewMatchers.withId(R.id.settingsUserPic))
            .check(ViewAssertions.matches(withDrawable(drawableRes)))
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
    @Test
    fun sendGalleryCorrectlyAddsChangePicture() {
        val expectedIntent: Matcher<Intent> = IntentMatchers.hasAction(Intent.ACTION_PICK)
        Intents.intending(expectedIntent).respondWith(storeImageAndSetResultStub())

        onView(ViewMatchers.withId(R.id.settingsUserPic))
            .check(ViewAssertions.matches(withDrawable(R.mipmap.ic_launcher_round)))

        onView(ViewMatchers.withId(R.id.changeProfilePictureButton)).perform(ViewActions.click())

        //get current user
        var user: User?
        runBlocking {
            user = UserRepository.getUserFromUID(
                firebaseAuth.currentUser!!.uid,
                firestore,
                firebaseAuth,
                firebaseStorage
            )
        }
        assertThat(user!!.getUserIconPath(),IsNot(IsNull.nullValue()))
    }

    // Helper function to create and stub an image
    private fun storeImageAndSetResultStub(): Instrumentation.ActivityResult {
        // create bitmap
        // below is a base64 blue image
        val encodedImage =
            "iVBORw0KGgoAAAANSUhEUgAAAKQAAACZCAYAAAChUZEyAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAG0SURBVHhe7dIxAcAgEMDALx4rqKKqDxZEZLhbYiDP+/17IGLdQoIhSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSEJmDnORA7zZz2YFAAAAAElFTkSuQmCC"
        val decodedImage = Base64.getDecoder().decode(encodedImage)
        image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)

        // create file
        val imageFile = File.createTempFile("dummyIMG", ".jpg")

        // store to outputstream
        val outputStream = FileOutputStream(imageFile)
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        // return with uri as data
        val resultData = Intent()
        val uri = Uri.fromFile(imageFile).toString()
        resultData.putExtra(PIC_ATTACHMENT_INTENT_KEY, uri)
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }

}
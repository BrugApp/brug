package com.github.brugapp.brug

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.di.sign_in.module.ActivityResultModule
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.ui.ProfilePictureSetActivity
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_USERNAME = "Rayan Kikou"

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

    private var testUserUid: String = ""
    private val TEST_PASSWORD: String = "123456"
    private val TEST_EMAIL: String = "unlost@profileUser.com"
    private val ACCOUNT1 = BrugSignInAccount("Rayan", "Kikou", "", "")

    @get:Rule
    val rule = HiltAndroidRule(this)

    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firebaseStorage: FirebaseStorage = FirebaseFakeHelper().providesStorage()

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
    private fun signInTestAccount(){
        runBlocking{
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
                    testUserUid,firestore)
        }
        firebaseAuth.signOut()
    }

    @Before
    fun setUp() {
        Intents.init()
        createTestUser()
        signInTestAccount()
        val intent = Intent(ApplicationProvider.getApplicationContext(), SettingsActivity::class.java)
        ActivityScenario.launch<ProfilePictureSetActivity>(intent)
    }

    @After
    fun cleanUp() {
        Intents.release()
        removeIconAndSignOut()
    }

    @Test
    fun correctNameIsDisplayed(){
        val name: String = TEST_USERNAME
        Thread.sleep(3000)
        Espresso.onView(ViewMatchers.withId(R.id.settingsUserName))
            .check(ViewAssertions.matches(ViewMatchers.withText(name)))
    }




}
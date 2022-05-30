package com.github.brugapp.brug

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.ui.ChatMenuActivity
import com.github.brugapp.brug.ui.QrCodeScannerActivity
import com.github.brugapp.brug.ui.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class QrCodeScannerActivityTest {
    @get:Rule
    var rule = HiltAndroidRule(this)

    @get:Rule
    var qrCodeScannerActivityRule = ActivityScenarioRule(QrCodeScannerActivity::class.java)

    @get:Rule
    val permissionRuleCoarseLocation: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    @get:Rule
    val permissionRuleFineLocation: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val permissionRuleCamera: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()

    @Before
    fun setUp(){
        Intents.init()
    }

    @After
    fun cleanUp(){
        Intents.release()
        if(firebaseAuth.currentUser != null){
            firebaseAuth.signOut()
        }
    }

    @Test
    fun hintTextIsCorrect(){
        onView(withId(R.id.edit_message))
            .check(matches((withHint("Report item…"))))
    }

    @Test
    fun reportWithEmptyTextFieldReturnsErrorToast(){
        val editTextItem = onView(withId(R.id.edit_message))
        editTextItem.perform(replaceText(""))
        onView(withId(R.id.buttonReportItem)).perform(click())

        // HERE SHOULD LIE AN ASSERTION ON TOAST MESSAGES, BUT IMPOSSIBLE TO DO
    }

    @Test
    fun canSeeHintWhenCameraIsClicked() {
        onView(withId(R.id.edit_message))
            .check(matches((withHint("Report item…"))))
    }

    @Test
    fun reportWithBadlyFormattedTextReturnsErrorToast() {
        val editTextItem = onView(withId(R.id.edit_message))
        editTextItem.perform(replaceText("abc"))
        onView(withId(R.id.buttonReportItem)).perform(click())
    }

    @Test
    fun reportWithValidQRStringAsAnonymousGoesToSignInActivity(){
        val userID = "ieieioOIaehhihuhimsjue"
        val itemID = "993uwjwjaiuhiu"
        runBlocking {
            UserRepository.addUserFromAccount(
                userID,
                BrugSignInAccount("Test", "User", "", ""),
                true,
                firestore
            )
            ItemsRepository.addItemWithItemID(
                Item("DummyItem", 0, "DummyDesc", true),
                itemID,
                userID,
                firestore
            )
        }

        val editTextItem = onView(withId(R.id.edit_message))
        editTextItem.perform(replaceText("$userID:$itemID"))
        closeSoftKeyboard()
        onView(withId(R.id.buttonReportItem)).perform(click())
        Thread.sleep(3000)
        intended(
            IntentMatchers.hasComponent(SignInActivity::class.java.name)
        )
    }


    @Test
    fun reportWithValidQRStringAsValidUserGoesToChatMenuActivity(){
        val userID = "woinaoxkqiuwhhweanh01109u921lk"
        val itemID = "993uwjwjaiuhiu"
        runBlocking {
            firebaseAuth.createUserWithEmailAndPassword("qrscanrealuser@test.com", "123456").await()
            firebaseAuth.signInWithEmailAndPassword("qrscanrealuser@test.com", "123456").await()
            UserRepository.addUserFromAccount(
                firebaseAuth.uid!!,
                BrugSignInAccount("MYNEW", "USER", "", ""),
                true,
                firestore
            )

            UserRepository.addUserFromAccount(
                userID,
                BrugSignInAccount("Test", "User", "", ""),
                true,
                firestore
            )

            ItemsRepository.addItemWithItemID(
                Item("DummyItem", 0, "DummyDesc", true),
                itemID,
                userID,
                firestore
            )
        }

        val editTextItem = onView(withId(R.id.edit_message))
        editTextItem.perform(replaceText("$userID:$itemID"))
        onView(withId(R.id.buttonReportItem)).perform(click())
        Thread.sleep(3000)
        intended(
            IntentMatchers.hasComponent(ChatMenuActivity::class.java.name)
        )
    }


}

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class QrCodeScannerActivityIntent{
    @get:Rule
    var rule = HiltAndroidRule(this)

    private val intent = Intent(ApplicationProvider.getApplicationContext(),QrCodeScannerActivity::class.java)

    @Test
    fun activityNoCrashOnResumeAndOnPause(){
        //https://stackoverflow.com/questions/61814729/is-there-a-way-to-pause-and-resume-activity-during-a-espresso-test
        val activityScenario = ActivityScenario.launch<QrCodeScannerActivity>(intent)
        // the activity's onCreate, onStart and onResume methods have been called at this point
        activityScenario.moveToState(Lifecycle.State.STARTED)
        // the activity's onPause method has been called at this point
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        // the activity's onResume method has been called at this point
    }
}
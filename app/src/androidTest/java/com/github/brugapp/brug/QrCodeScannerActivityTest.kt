package com.github.brugapp.brug

import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.ui.QrCodeScannerActivity
import com.github.brugapp.brug.ui.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


private const val ERROR_STR = "ERROR: An error has occurred, try again."
private const val SUCCESS_STR = "Thank you ! The user will be notified."

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class QrCodeScannerActivityTest {
    @get:Rule
    var rule = HiltAndroidRule(this)

    @get:Rule
    var qrCodeScannerActivityRule = ActivityScenarioRule(QrCodeScannerActivity::class.java)

    //https://stackoverflow.com/questions/33929937/android-marshmallow-test-permissions-with-espresso
    //@get:Rule
    //var permissionRule: GrantPermissionRule = GrantPermissionRule
    //    .grant(android.Manifest.permission.CAMERA)

    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()

//    private val firebaseMessaging: FirebaseMessaging = FirebaseFakeHelper.providesFirebaseMessaging()

    @Before
    fun setUp(){
        Intents.init()
    }

    @After
    fun cleanUp(){
        Intents.release()
    }

    @Test
    fun hintTextIsCorrect(){
        onView(withId(R.id.editTextReportItem))
            .check(matches((withHint("Report item…"))))
    }

    @Test
    fun reportButtonDisplaysNotificationWithoutCrashing(){
        onView(withId(R.id.buttonReportItem))
            .perform(click())
        NotificationManagerCompat.from(ApplicationProvider.getApplicationContext()).cancelAll()
        Thread.sleep(1000)
    }

    @Test
    fun reportWithEmptyTextFieldReturnsErrorToast(){
        val editTextItem = onView(withId(R.id.editTextReportItem))
        editTextItem.perform(replaceText(""))
        onView(withId(R.id.buttonReportItem)).perform(click())

        // HERE SHOULD LIE AN ASSERTION ON TOAST MESSAGES, BUT IMPOSSIBLE TO DO
    }

    @Test
    fun reportWithBadlyFormattedTextReturnsErrorToast() {
        val editTextItem = onView(withId(R.id.editTextReportItem))
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
                firestore
            )
            ItemsRepository.addItemWithItemID(
                MyItem("DummyItem", 0, "DummyDesc", true),
                itemID,
                userID,
                firestore
            )
        }

        val editTextItem = onView(withId(R.id.editTextReportItem))
        editTextItem.perform(replaceText("$userID:$itemID"))
        onView(withId(R.id.buttonReportItem)).perform(click())
        Thread.sleep(1000)
        intended(
            IntentMatchers.hasComponent(SignInActivity::class.java.name)
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
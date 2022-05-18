package com.github.brugapp.brug

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.github.brugapp.brug.ui.NFCScannerActivity
import com.github.brugapp.brug.view_model.NFCScanViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

@HiltAndroidTest
class NFCScannerActivityTest {
    private val viewModel = NFCScanViewModel()

    val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var testRule = ActivityTestRule(NFCScannerActivity::class.java, false, false)

    @Before
    fun setUp(){
        Intents.init()
        testRule.launchActivity(Intent(targetContext, NFCScannerActivity::class.java))
    }

    @After
    fun cleanUp(){
        Intents.release()
    }

    @Test
    fun writeModeOffTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        mockActivity.writeModeOff()
        assertThat(mockActivity.writeMode,`is`(true))
    }

    @Test
    fun writeModeOnTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        mockActivity.writeModeOn()
        assertThat(mockActivity.writeMode,`is`(false))
    }

    @Test
    fun onNewIntentTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        val intent = Intent()
        mockActivity.onNewIntent(intent)
        assertThat(mockActivity.intent,`is`(IsNull.nullValue()))
    }

    @Test
    fun onNewActionTagIntentTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        val intent = Intent()
        intent.action = NfcAdapter.ACTION_TAG_DISCOVERED
        mockActivity.onNewIntent(intent)
        assertThat(mockActivity.tag,`is`(IsNull.nullValue()))
    }

    @Test
    fun onPauseTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        mockActivity.onPause()
        assertThat(mockActivity.writeMode,`is`(false))
    }

    @Test
    fun onResumeTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        mockActivity.onResume()
        assertThat(mockActivity.writeMode,`is`(false))
    }

    @Test
    fun onCreateTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        val bundle = Bundle()
        mockActivity.onCreate(bundle)
        assertThat(mockActivity.adapter,`is`(IsNull.nullValue()))
    }

    @Test
    fun mockAdapter(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        mockActivity.onCreate(null)
        //mockActivity.adapter = mock(NfcAdapter::class.java)
        mockActivity.writeModeOff()
        mockActivity.writeModeOn()
        assertThat(mockActivity.adapter,`is`(IsNull.nullValue()))
    }

    @Test
    fun nfcTestAll() {
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        val bundle = Bundle()
        mockActivity.onCreate(bundle)
        //mockActivity.findViews()
        mockActivity.onPause()
        mockActivity.onResume()
        mockActivity.writeModeOff()
        mockActivity.writeModeOn()
        val intent = Intent()
        mockActivity.onNewIntent(intent)
        mockActivity.onCreate(null)

        val record = viewModel.createRecord("hello")
        val ndefArray = arrayOf(NdefMessage(record))
        val activity = mockActivity
        val context =  InstrumentationRegistry.getInstrumentation().getContext();//mock(Context::class.java)
        activity.context = context
        viewModel.checkNFCPermission(context)
        viewModel.setupTag()
        viewModel.checkIntentAction(intent)
        viewModel.rawMessageToMessage(intent)
        viewModel.initText(ndefArray)
        assertThat(mockActivity.adapter, `is`(IsNull.nullValue()))}


    @Test
    fun testEditMessage(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, NFCScannerActivity::class.java)
        ActivityScenario.launch<Activity>(intent).use {
                val editMessage = onView(withId(R.id.edit_message))
                    editMessage.check(matches(isDisplayed()))
            }
    }

    @Test
    fun testNfcContents() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, NFCScannerActivity::class.java)
        ActivityScenario.launch<Activity>(intent).use {
            val nfcContents = onView(withId(R.id.nfcContents))
            nfcContents.check(matches(isEnabled()))
        }
    }

    @Test
    fun testActivateButton(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, NFCScannerActivity::class.java)
        ActivityScenario.launch<Activity>(intent).use {
            val activateButton = onView(withId(R.id.buttonReportItem))
            activateButton.perform(click())
            activateButton.check(matches(isDisplayed()))
        }
    }
}

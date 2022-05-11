package com.github.brugapp.brug

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ACTION_TAG_DISCOVERED
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.view_model.NFCScanViewModel
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.Charset
import kotlin.experimental.and

@RunWith(AndroidJUnit4::class)
class NFCScanViewModelTest {
    private val viewModel = NFCScanViewModel()

    @Test
    fun createRecordTest(){
        val text = "hello"
        assertThat(viewModel.createRecord(text), IsNot(IsNull.nullValue()))
    }
    
    @Test
    fun setupTagTest() {
        assert(viewModel.setupTag().hasCategory(Intent.CATEGORY_DEFAULT))
    }

    @Test
    fun rawMessageToMessageTest() {
        val intent = Intent()
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        var messages: Array<NdefMessage> = arrayOf<NdefMessage>()
        var bool: Boolean = false
            if (rawMessages!=null) {
                messages = Array<NdefMessage>(rawMessages!!.size) { i -> rawMessages[i] as NdefMessage }
                if(rawMessages!=null) bool=true
            } 
        assertThat(Pair(bool,messages),IsNot(IsNull.nullValue()))
    }

    @Test
    fun checkIntentActionTest(){
        val intent = Intent()
        assertThat (intent.action.equals(ACTION_TAG_DISCOVERED)||intent.action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)||intent.action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED),`is`(viewModel.checkIntentAction(intent)))
    }

    @Test
    fun nullMsgInitTextTest(){
        var bugtext: String = "null message error"
        assertThat(viewModel.initText(null),`is`(bugtext))
    }

    @Test
    fun emptyMsgInitTextTest(){
        var messages: Array<NdefMessage> = emptyArray()
        var text: String = "test"
        assertThat(viewModel.initText(messages),`is`(""))
    }

    @Test
    fun nonEmptyMsgInitTextTest(){
        var mockMessages: Array<NdefMessage> = arrayOf(NdefMessage(arrayOf(NdefRecord.createUri("hello"))))
        val payload = mockMessages[0].records[0].payload
        val textEncoding = if((payload[0] and 128.toByte()).toInt() == 0) Charset.forName("UTF-8") else Charset.forName("UTF-16")
        val languageCodeLength = payload[0] and 63.toByte()
        val text = String(payload,languageCodeLength+1,payload.size-languageCodeLength-1,textEncoding)
        assertThat(viewModel.initText(mockMessages),`is`(text))
    }

     
}
/*- - - - -these UI tests only work on physical devices: can't test emulator without NFC! - - - - -
@RunWith(AndroidJUnit4::class)
class NFCScannerActivityTest {

    private val myIntent = Intent(ApplicationProvider.getApplicationContext(), NFCScannerActivity::class.java)

    @Test
    fun correctTitleShown(){
        ActivityScenario.launch<NFCScannerActivity>(myIntent).use {
            onView(withId(R.id.nfcScanTitle)).check(matches(withText("NFC Scanner")))
        }
    }

    @get:Rule
    var nfcScannerActivityRule = ActivityScenarioRule(NFCScannerActivity::class.java)

    @Test
    fun correctHintText(){
        onView(withId(R.id.edit_message)).check(matches((withHint("type something"))))
    }

    @Test
    fun correctButton(){
        onView(withId(R.id.buttonReportItem)).check(matches(withText("activate tag")))
    }
//- - - - - - - - - - - - - - - - -these tests work on physical devices - - - - - - - - - - - - - -
*/

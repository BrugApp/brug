package com.github.brugapp.brug

import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ACTION_TAG_DISCOVERED
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.brugapp.brug.ui.NFCScannerActivity
import com.github.brugapp.brug.view_model.NFCScanViewModel
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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

    @Test
    fun testRecordCreation(){
        val activity: NFCScannerActivity = Mockito.mock(NFCScannerActivity::class.java)
        val context = InstrumentationRegistry.getInstrumentation().context
        val record = viewModel.createRecord("hello")
        viewModel.checkNFCPermission(context)
        viewModel.setupTag()
        assertThat(record,`is`(viewModel.createRecord("hello")))
    } 
}

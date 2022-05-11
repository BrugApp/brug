package com.github.brugapp.brug.view_model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.*
import android.nfc.NfcAdapter.ACTION_TAG_DISCOVERED
import android.nfc.tech.Ndef
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import kotlin.experimental.and

private const val NFC_REQUEST_CODE = 1000101

class NFCScanViewModel : ViewModel() {
    fun checkNFCPermission(context1: Context){ if(PackageManager.PERMISSION_DENIED == checkSelfPermission(context1,Manifest.permission.NFC)) requestPermissions(context1 as Activity, arrayOf(Manifest.permission.NFC), NFC_REQUEST_CODE) }

    fun setupAdapter(this1: Context): NfcAdapter { return NfcAdapter.getDefaultAdapter(this1) }

    fun setupWritingTagFilters(this1: Context): Pair<PendingIntent,Array<IntentFilter>>{ return Pair(PendingIntent.getActivity(this1, 0, Intent(this1, this1.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), FLAG_MUTABLE),arrayOf(setupTag())) }

    fun setupTag(): IntentFilter {
        //returns: IntentFilter with an added default category
        //params: none
        //use: prepares the IntentFilter needed to produce the 2nd Pair element of setupWritingTagFilters

        val tagDetected = IntentFilter(ACTION_TAG_DISCOVERED)
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT)

        return tagDetected
     }

    fun readFromIntent(nfcContents: TextView, intent: Intent){ if (checkIntentAction(intent)&&rawMessageToMessage(intent).first){ buildTagViews(nfcContents, rawMessageToMessage(intent).second) } }

    fun checkIntentAction(intent: Intent): Boolean {
        //returns: true when the intent action corresponds to a tag being discovered
        //         false otherwise
        //params: 'intent' used to get the associated action for testing if tag is discovered
        //use: combines 3 condition checks into a single boolean for calling readFromIntent

        val isACTIONTAGDISCOVERED = intent.action.equals(ACTION_TAG_DISCOVERED)
        val isACTIONTECHDISCOVERED = intent.action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)
        val isACTIONNDEFDISCOVERED = intent.action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)

        return (isACTIONTAGDISCOVERED||isACTIONTECHDISCOVERED||isACTIONNDEFDISCOVERED)
    }

    fun rawMessageToMessage(intent: Intent): Pair<Boolean,Array<NdefMessage>> {
        //returns 2 objects:
            //Boolean: this object checks if the extended data from intent is null
            //Array<NdefMessage>: this object represented the extended data from intent
        //params: 'intent' is used for getting extended data from this intent
        //use: sets up the data and conditions needed for calling buildTagViews

        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        var messages: Array<NdefMessage> = arrayOf<NdefMessage>()
        var bool: Boolean = false

        if (rawMessages!=null) {
            messages = Array<NdefMessage>(rawMessages!!.size) {
                    i -> rawMessages[i] as NdefMessage
            }
            if(rawMessages!=null){
                bool = true
            }
        }

        return Pair(bool,messages)
    }

    @SuppressLint("SetTextI18n")
    private fun buildTagViews(nfcContents: TextView, messages: Array<NdefMessage>){ try{ nfcContents.text = "Read Tag Contents:" + initText(messages) }catch (e : UnsupportedEncodingException){ Log.e("UnsupportedEncoding",e.toString()) } }
    
    @Throws(UnsupportedEncodingException::class)
    fun initText(messages: Array<NdefMessage>): String {
        //returns: String computed from the tag's NdefMessage Array
        //params: 'messages'  is an array of NdefMessages, a lightweight binary format for tags
        //use: converts NdefMessage Array into a String message understandable to humans

        var text: String = ""

        if (messages==null || messages.isEmpty()){
            return text
        }

        val payload = messages[0].records[0].payload
        val textEncoding =
            if((payload[0] and 128.toByte()).toInt() == 0){
                Charset.forName("UTF-8")
            } else{
                Charset.forName("UTF-16")
            }

        val languageCodeLength = payload[0] and 63.toByte()

        text = String(payload,languageCodeLength+1,payload.size-languageCodeLength-1,textEncoding)

        return text
    }

    @Throws(IOException::class, FormatException::class)
    fun write(text: String, tag: Tag) { Ndef.get(tag).connect()
        Ndef.get(tag).writeNdefMessage(NdefMessage(arrayOf(createRecord(text))))
        Ndef.get(tag).close() }

    @Throws(UnsupportedEncodingException::class)
    fun createRecord(text: String): NdefRecord {
        //returns: an immutable NdefRecord

        //parameters: 'text' the string message we want to build the record from

        //use: the write function calls createRecord in order to write a given message to the tag

        val lang = "en"
        //we can change the record language in the previous line

        val textBytes = text.toByteArray()
        val langBytes = lang.toByteArray()

        val langLength = langBytes.size
        val textLength = textBytes.size

        var payload: ByteArray = ByteArray(
            langLength+textLength+1)

        payload[0] = langLength.toByte()

        System.arraycopy(
            langBytes,
            0,
            payload,
            1,
            langLength
        )

        System.arraycopy(
            textBytes,
            0,
            payload,
            1 + langLength,
            textLength
        )

        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), payload)

    }

    fun displayReportNotification(this1: Context) { MyFCMMessagingService.sendNotification(this1, "Item found", "One of your items was found!") }
}

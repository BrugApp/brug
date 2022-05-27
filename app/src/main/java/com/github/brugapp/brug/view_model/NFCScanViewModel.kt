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
    /**
     * makes sure we have NFC permissions
     * @param context: the current context from which we check permissions
     */
    fun checkNFCPermission(context: Context){ if(PackageManager.PERMISSION_DENIED == checkSelfPermission(context,Manifest.permission.NFC)) requestPermissions(context as Activity, arrayOf(Manifest.permission.NFC), NFC_REQUEST_CODE) }

    /**
     * initializes Nfc adapter if it exists
     * @param thisContext: the current context from which we want to set up the Nfc adapter
     * @return
     */
    fun setupAdapter(thisContext: Context): NfcAdapter? { return try{ NfcAdapter.getDefaultAdapter(thisContext) }catch(e: Exception){ null } }

    /**
     * prepares the Intent needed to call setupTag()
     * @param this1
     * @return Pair<PendingIntent,Array<IntentFilter>>
     */
    fun setupWritingTagFilters(thisContext: Context): Pair<PendingIntent,Array<IntentFilter>>{ return Pair(PendingIntent.getActivity(thisContext, 0, Intent(thisContext, thisContext.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), FLAG_MUTABLE),arrayOf(setupTag())) }

    /**
     * sets up an IntentFilter that matches against the action of tag discovery
     * @use prepares the IntentFilter needed to produce the 2nd Pair element of setupWritingTagFilters
     * @return IntentFilter with an added default category
     */
    fun setupTag(): IntentFilter {
        val tagDetected = IntentFilter(ACTION_TAG_DISCOVERED)
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT)
        return tagDetected
    }

    /**
     * we read the Nfc tag and output the message in the textview parameter
     * @param nfcContents
     * @param intent
     */
    fun readFromIntent(nfcContents: TextView, intent: Intent){ if (checkIntentAction(intent)&&rawMessageToMessage(intent).first){ buildTagViews(nfcContents, rawMessageToMessage(intent).second) } }

    /**
     * makes sure an appropriate action justifies calling readFromIntent
     * @use combines 3 condition checks into a single boolean for calling readFromIntent
     * @param intent: used to get the associated action for testing if tag is discovered
     * @return true when the intent action corresponds to a tag being discovered false otherwise
     */
    fun checkIntentAction(intent: Intent): Boolean {
        val isACTIONTAGDISCOVERED = intent.action.equals(ACTION_TAG_DISCOVERED)
        val isACTIONTECHDISCOVERED = intent.action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)
        val isACTIONNDEFDISCOVERED = intent.action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)
        return (isACTIONTAGDISCOVERED || isACTIONTECHDISCOVERED || isACTIONNDEFDISCOVERED)
    }

    /**
     * builds the array of Ndefmessages from the Nfc adapter
     * @use sets up the data and conditions needed for calling buildTagViews
     * @param intent: used for getting extended data from this intent
     * @return Boolean: this object checks if the extended data from intent is null
     * @return Array<NdefMessage>: this object represented the extended data from intent
     */
    fun rawMessageToMessage(intent: Intent): Pair<Boolean,Array<NdefMessage>> {

        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        var messages: Array<NdefMessage> = arrayOf()
        var bool = false

        if (rawMessages!=null) {
            messages = Array(rawMessages.size) { i -> rawMessages[i] as NdefMessage }
            bool = true
        }
        return Pair(bool,messages)
    }

    /**
     * set a Textview text to a String converted from an array of NdefMessages
     * @use converts NdefMessage Array into a String message understandable to humans
     * @param nfcContents: the textview that contains the read nfc text
     * @param messages is an array of NdefMessages, a lightweight binary format for tags
     * @return String computed from the tag's NdefMessage Array
     */
    @SuppressLint("SetTextI18n")
    fun buildTagViews(nfcContents: TextView, messages: Array<NdefMessage>){ try{ nfcContents.text = initText(messages) }catch (e : UnsupportedEncodingException){ Log.e("UnsupportedEncoding",e.toString()) } }

    @Throws(UnsupportedEncodingException::class)
    fun initText(messages: Array<NdefMessage>?): String {
        val bugText = "null message error"
        val text = ""
        return when {
            messages==null -> { bugText }
            messages.isEmpty() -> { text }
            else -> {
                val payload = messages[0].records[0].payload
                val textEncoding =
                    if ((payload[0] and 128.toByte()).toInt() == 0) Charset.forName("UTF-8") else Charset.forName("UTF-16")
                val languageCodeLength = payload[0] and 63.toByte()
                String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, textEncoding)
            }
        }
    }

    /**
     * writes a given text to the given tag
     * @param text: text we want to write
     * @param tag: tag to whom we want to write
     */
    @Throws(IOException::class, FormatException::class)
    fun write(text: String, tag: Tag) { Ndef.get(tag).connect()
        Ndef.get(tag).writeNdefMessage(NdefMessage(arrayOf(createRecord(text))))
        Ndef.get(tag).close() }

    /**
     * the write function calls createRecord to write a given message to the tag
     * @param text: the string message we want to build the record from
     * @return an immutable NdefRecord
     */
    @Throws(UnsupportedEncodingException::class)
    fun createRecord(text: String): NdefRecord {
        val lang = "en"
        //we can change the record language in the previous line
        val textBytes = text.toByteArray()
        val langBytes = lang.toByteArray()
        val langLength = langBytes.size
        val textLength = textBytes.size
        val payload = ByteArray(langLength+textLength+1)
        payload[0] = langLength.toByte()
        System.arraycopy(langBytes, 0, payload, 1, langLength)
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength)
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), payload)
    }

    /**
     * @use notify owner that their item is found
     * @param thisContext
     */
    fun displayReportNotification(thisContext: Context) { MyFCMMessagingService.sendNotification(thisContext, "Item found", "One of your items was found!") }
}
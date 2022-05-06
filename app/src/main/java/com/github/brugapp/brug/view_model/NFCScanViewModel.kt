package com.github.brugapp.brug.view_model

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.nfc.*
import android.nfc.tech.Ndef
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import kotlin.experimental.and
import kotlin.jvm.Throws

private const val NFC_REQUEST_CODE = 1000101

class NFCScanViewModel : ViewModel() {
    fun checkPermission(context: Context){
        val perm = ContextCompat.checkSelfPermission(context,Manifest.permission.NFC)
        if(perm == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(Manifest.permission.NFC), NFC_REQUEST_CODE)
    }

    fun setupAdapter(this1: Context): NfcAdapter{
        return NfcAdapter.getDefaultAdapter(this1)
    }

    fun setupWritingTagFilters(this1: Context): Pair<PendingIntent,Array<IntentFilter>>{
        var nfcIntent = PendingIntent.getActivity(this1, 0, Intent(this1, this1.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        var tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT)
        return Pair(nfcIntent,arrayOf(tagDetected))
    }

    fun readFromIntent(nfcContents: TextView, intent: Intent){
        val action = intent.action
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)||action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)||action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            lateinit var messages : Array<NdefMessage>
            if(rawMessages!=null){
                for(i in rawMessages.indices){
                    messages[i]=rawMessages[i] as NdefMessage
                }
            }
            buildTagViews(nfcContents, messages)
        }
    }

    private fun buildTagViews(nfcContents: TextView, messages: Array<NdefMessage>){
        if (messages==null|| messages.isEmpty()) return
        lateinit var text : String
        var payload = messages[0].records[0].payload
        var textEncoding = if((payload[0] and 128.toByte()).toInt() == 0) Charset.forName("UTF-8") else Charset.forName("UTF-16")
        var languageCodeLength = payload[0] and 63.toByte()
        try{
            text = String(payload,languageCodeLength+1,payload.size-languageCodeLength-1,textEncoding)
        }catch (e : UnsupportedEncodingException){
            Log.e("UnsupportedEncoding",e.toString())
        }
        nfcContents.text = "NFC Contents: $text"
    }

    @Throws(IOException::class, FormatException::class)
    fun write(text: String, tag: Tag) {
        val records = arrayOf(createRecord(text))
        val message = NdefMessage(records)
        val ndef = Ndef.get(tag)
        ndef.connect()
        ndef.writeNdefMessage(message)
        ndef.close()
    }

    @Throws(UnsupportedEncodingException::class)
    private fun createRecord(text: String): NdefRecord {
        val lang = "en"
        val textBytes = text.toByteArray()
        val langBytes = lang.toByteArray()
        val langLength = langBytes.size
        val textLength = textBytes.size
        lateinit var payload: ByteArray
        payload[0] = langLength.toByte()
        System.arraycopy(langBytes, 0, payload, 1, langLength)
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength)
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), payload)
    }

    fun displayReportNotification(this1: Context) {
        MyFCMMessagingService.sendNotification(this1, "Item found", "One of your items was found!")
    }
}
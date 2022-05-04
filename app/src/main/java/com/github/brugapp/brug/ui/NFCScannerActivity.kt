package com.github.brugapp.brug.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.*
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import com.github.brugapp.brug.view_model.NFCScanViewModel
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import kotlin.experimental.and
import kotlin.jvm.Throws

class NFCScannerActivity: AppCompatActivity() {
    private val Error_detected = "No NFC was found!"
    private val Write_success = "Text written successfully"
    private val Write_error = "Error occurred during writing, try again"

    private lateinit var adapter: NfcAdapter
    private lateinit var nfcIntent: PendingIntent
    private lateinit var writingTagFilters: Array<IntentFilter>
    private var writeMode: Boolean = false
    private lateinit var tag: Tag
    private lateinit var context: Context
    private lateinit var nfcInfo: TextView
    private lateinit var nfcContents: TextView
    private lateinit var activateButton: Button

    private val viewModel: NFCScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_scanner)
        viewModel.checkPermission(this)
        adapter = NfcAdapter.getDefaultAdapter(this)
        nfcInfo = findViewById<View>(R.id.editTextReportItem) as TextView
        nfcContents = findViewById<View>(R.id.editTextTextPersonName) as TextView
        activateButton = findViewById<View>(R.id.buttonReportItem) as Button
        context = this

        activateButton.setOnClickListener{
            try{
                if(tag==null){
                    Toast.makeText(context,Error_detected,Toast.LENGTH_LONG).show()
                }else{
                    write("Plaintext|"+nfcInfo.getText().toString(),tag)
                    Toast.makeText(context,Write_success,Toast.LENGTH_LONG).show()
                }
            }catch(e: IOException){
                Toast.makeText(context,Write_error,Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }catch(e: FormatException){
                Toast.makeText(context,Write_error,Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            displayReportNotification()
        }

        if (adapter==null || adapter.isEnabled){
            Toast.makeText(this,"This device doesn't support NFC!",Toast.LENGTH_SHORT).show()
            finish()
        }else {
            readFromIntent(getIntent())
            nfcIntent = PendingIntent.getActivity(this, 0, Intent(this, this.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
            var tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
            tagDetected.addCategory(Intent.CATEGORY_DEFAULT)
            writingTagFilters = arrayOf(tagDetected)
        }
    }

    private fun readFromIntent(intent: Intent){
        val action = intent.action
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)||action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)||action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            lateinit var messages : Array<NdefMessage>
            if(rawMessages!=null){
               for(i in rawMessages.indices){
                   messages[i]=rawMessages[i] as NdefMessage
               }
            }
            buildTagViews(messages)
        }
    }

    private fun buildTagViews(messages: Array<NdefMessage>){
        if (messages==null|| messages.isEmpty()) return
        lateinit var text : String
        var payload = messages[0].records[0].payload
        var textEncoding = if((payload[0] and 128.toByte()) == 0 as Byte) Charset.forName("UTF-8") else Charset.forName("UTF-16")
        var languageCodeLength = payload[0] and 63.toByte()
        try{
            text = String(payload,languageCodeLength+1,payload.size-languageCodeLength-1,textEncoding)
        }catch (e : UnsupportedEncodingException){
            Log.e("UnsupportedEncoding",e.toString())
        }
        nfcContents.text = "NFC Contents: $text"
    }

    @Throws(IOException::class,FormatException::class)
    private fun write(text: String, tag: Tag) {
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
        payload[0] = langLength as Byte
        System.arraycopy(langBytes, 0, payload, 1, langLength)
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength)
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), payload)
    }

    private fun displayReportNotification() {
        MyFCMMessagingService.sendNotification(this, "Item found",
            "One of your items was found!")
    }

    override fun onPause() {
        super.onPause()
        writeModeOff()
    }

    override fun onResume(){
        super.onResume()
        writeModeOn()
    }

    private fun writeModeOff(){
        writeMode = true
        adapter.disableForegroundDispatch(this)
    }

    private fun writeModeOn(){
        adapter.enableForegroundDispatch(this,nfcIntent,writingTagFilters,null)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readFromIntent(intent)
        if ((NfcAdapter.ACTION_TAG_DISCOVERED) == intent.action){
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)!!
        }
    }
}
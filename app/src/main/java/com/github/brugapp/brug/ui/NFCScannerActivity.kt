package com.github.brugapp.brug.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import com.github.brugapp.brug.view_model.NFCScanViewModel

class NFCScannerActivity: AppCompatActivity() {
    private val Error_detected = "No NFC was found!"
    private val Write_success = "Text written successfully"
    private val Write_error = "Error occurred during writing, try again"

    private lateinit var adapter: NfcAdapter
    private lateinit var nfcIntent: PendingIntent
    private lateinit var writingTagFilters: IntentFilter
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
            displayReportNotification()
        }

        if (adapter==null && adapter.isEnabled){
            nfcIntent = PendingIntent.getActivity(this,0,Intent(this,this.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0)
        }else{
            nfcInfo.setText(Error_detected)
        }
    }
    private fun displayReportNotification() {
        MyFCMMessagingService.sendNotification(this, "Item found",
            "One of your items was found!")
    }

    override fun onPause() {
        adapter.disableForegroundDispatch(this)
        super.onPause()
    }

    override fun onResume(){
        super.onResume()
        adapter.enableForegroundDispatch(this,nfcIntent,null,null)
    }
}
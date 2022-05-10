package com.github.brugapp.brug.ui

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.FormatException
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ACTION_TAG_DISCOVERED
import android.nfc.Tag
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.NFCScanViewModel
import java.io.IOException

class NFCScannerActivity: AppCompatActivity() {
    private val Error_detected = "No NFC was found!"
    private val Write_success = "Text written successfully"
    private val Write_error = "Error occurred during writing, try again"
    private val viewModel: NFCScanViewModel by viewModels()
    private var writeMode: Boolean = false
    private var tag: Tag? = null
    private lateinit var adapter: NfcAdapter
    private lateinit var nfcIntent: PendingIntent
    private lateinit var writingTagFilters: Array<IntentFilter>
    private lateinit var editMessage: TextView
    private lateinit var nfcContents: TextView
    private lateinit var activateButton: Button

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_scanner)
        viewModel.checkNFCPermission(this)
        adapter = viewModel.setupAdapter(this)
        editMessage = findViewById<View>(R.id.edit_message) as TextView
        nfcContents = findViewById<View>(R.id.nfcContents) as TextView
        activateButton = findViewById<View>(R.id.buttonReportItem) as Button
        if (adapter==null){ Toast.makeText(this,"This device doesn't support NFC!",Toast.LENGTH_SHORT).show()
            finish() }
        nfcIntent = viewModel.setupWritingTagFilters(this).first
        writingTagFilters = viewModel.setupWritingTagFilters(this).second
        activateButton.setOnClickListener{
            try{ if(tag==null) Toast.makeText(this,Error_detected,Toast.LENGTH_LONG).show()
                else{ viewModel.write(editMessage.text.toString(),tag!!)
                    Toast.makeText(this,Write_success,Toast.LENGTH_LONG).show() }
            }catch(e: Exception){
                when(e){ is IOException, is FormatException ->{ Toast.makeText(this,Write_error,Toast.LENGTH_LONG).show()
                        e.printStackTrace() }
                    else -> throw e } }
            viewModel.displayReportNotification(this) } }

    override fun onPause() {
        super.onPause()
        writeModeOff() }

    override fun onResume(){
        super.onResume()
        writeModeOn() }

    private fun writeModeOff(){
        writeMode = true
        adapter.disableForegroundDispatch(this) }

    private fun writeModeOn(){
        adapter.enableForegroundDispatch(this,nfcIntent,writingTagFilters,null) }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.readFromIntent(nfcContents,intent)
        if ((ACTION_TAG_DISCOVERED) == intent.action){ tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)!! } }
}
package com.github.brugapp.brug.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.*
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
    private var writeMode: Boolean = false

    private lateinit var adapter: NfcAdapter
    private lateinit var nfcIntent: PendingIntent
    private lateinit var writingTagFilters: Array<IntentFilter>
    private var tag: Tag? = null
    private lateinit var context: Context
    private lateinit var editMessage: TextView
    private lateinit var nfcContents: TextView
    private lateinit var activateButton: Button
    private val viewModel: NFCScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_scanner)
        context = this
        viewModel.checkPermission(context)
        adapter = viewModel.setupAdapter(context)
        print("adapter is $adapter")
        editMessage = findViewById<View>(R.id.edit_message) as TextView
        nfcContents = findViewById<View>(R.id.nfcContents) as TextView
        activateButton = findViewById<View>(R.id.buttonReportItem) as Button

        if (adapter==null){ //|| adapter.isEnabled){
            Toast.makeText(this,"This device doesn't support NFC!",Toast.LENGTH_SHORT).show()
            finish()
        }
        nfcIntent = viewModel.setupWritingTagFilters(this).first
        writingTagFilters = viewModel.setupWritingTagFilters(this).second

        activateButton.setOnClickListener{
            try{
                if(tag==null){
                    Toast.makeText(context,Error_detected,Toast.LENGTH_LONG).show()
                }else{
                    viewModel.write("Plaintext|"+editMessage.text.toString(),tag!!) //changed tag to tag!!
                    Toast.makeText(context,Write_success,Toast.LENGTH_LONG).show()
                }
            }catch(e: IOException){
                Toast.makeText(context,Write_error,Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }catch(e: FormatException){
                Toast.makeText(context,Write_error,Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            viewModel.displayReportNotification(context)
        }
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
        viewModel.readFromIntent(nfcContents,intent)
        if ((NfcAdapter.ACTION_TAG_DISCOVERED) == intent.action){
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)!!
            Toast.makeText(context,"action tag was discovered",Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(context,"action tag was not discovered",Toast.LENGTH_LONG).show()
        }
    }
}
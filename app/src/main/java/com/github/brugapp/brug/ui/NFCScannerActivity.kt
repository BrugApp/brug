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

open class NFCScannerActivity: AppCompatActivity() {
    private val Error_detected = "No NFC was found!"
    private val Write_success = "Text written successfully"
    private val Write_error = "Error occurred during writing, try again"
    private val viewModel: NFCScanViewModel by viewModels()
    var writeMode: Boolean = false
    var tag: Tag? = null
    var adapter: NfcAdapter? = null
    private lateinit var nfcIntent: PendingIntent
    private lateinit var writingTagFilters: Array<IntentFilter>
    var editMessage: TextView? = null
    private var nfcContents: TextView? = null
    private var activateButton: Button? = null

    public override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_scanner)
        viewModel.checkNFCPermission(this)
        adapter = viewModel.setupAdapter(this)
        findViews() //maybe return early if false
        if (adapter==null){ Toast.makeText(this,"This device doesn't support NFC!",Toast.LENGTH_SHORT).show()
            finish() }
        nfcIntent = viewModel.setupWritingTagFilters(this).first
        writingTagFilters = viewModel.setupWritingTagFilters(this).second
        activateButton!!.setOnClickListener{
            try{ if(tag==null) Toast.makeText(this,Error_detected,Toast.LENGTH_LONG).show()
            else{ viewModel.write(editMessage!!.text.toString(),tag!!)
                Toast.makeText(this,Write_success,Toast.LENGTH_LONG).show() }
            }catch(e: Exception){ when(e){ is IOException, is FormatException ->{ Toast.makeText(this,Write_error,Toast.LENGTH_LONG).show()
                e.printStackTrace() }
                else -> throw e } }
            viewModel.displayReportNotification(this) } }

    fun findViews(): Boolean{
        //returns true iff all textviews & buttons are found
        //params: None
        //use: abbreviates the onCreate method

        editMessage = findViewById<View>(R.id.edit_message) as TextView?
        nfcContents = findViewById<View>(R.id.nfcContents) as TextView?
        activateButton = findViewById<View>(R.id.buttonReportItem) as Button?

        return editMessage!=null && nfcContents!=null && activateButton!=null

    }
    public override fun onPause() { super.onPause()
        writeModeOff() }

    public override fun onResume(){ super.onResume()
        writeModeOn() }

    fun writeModeOff(){
        //allows us to stop writing to NFC tag when app is paused

        writeMode = true

        if(adapter!=null) {
            adapter!!.disableForegroundDispatch(this)
        }

    }

    fun writeModeOn(){
        //allows us to write to NFC tag as long as app is started/resumed

        if(adapter!=null) {
            adapter!!.enableForegroundDispatch(this,nfcIntent,writingTagFilters,null)
        }

    }

    public override fun onNewIntent(intent: Intent) {
        //this allows us to assign a tag for a corresponding intent

        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.readFromIntent(nfcContents!!,intent)
        if ((ACTION_TAG_DISCOVERED) == intent.action){
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)!!
        }
    }
}
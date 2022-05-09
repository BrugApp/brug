package com.github.brugapp.brug.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ConvRepository
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import com.github.brugapp.brug.view_model.QrCodeScanViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking


//library found on github: https://github.com/yuriy-budiyev/code-scanner
class QrCodeScannerActivity : AppCompatActivity() {

    private val viewModel: QrCodeScanViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)
        viewModel.checkPermission(this)
        viewModel.codeScanner(this)

        //TODO: REMOVE THIS HARDCODED TEXT
        findViewById<EditText>(R.id.editTextReportItem).setText("ZXlQaXEsH9Zpme64QtnhNZCnt6Y2:6dKDuJTL0VCCxAPFClUZ")

        findViewById<Button>(R.id.buttonReportItem).setOnClickListener {
            val qrContentBox = findViewById<EditText>(R.id.editTextReportItem)
            if(qrContentBox.text.isNullOrBlank()){
                Snackbar.make(it,
                    "ERROR: you must scan the QR code before reporting",
                    Snackbar.LENGTH_LONG)
                    .show()
            } else if (!qrContentBox.text.contains(":")){
                Snackbar.make(it,
                    "ERROR: the QR code you scanned contains badly formatted data",
                    Snackbar.LENGTH_LONG)
                    .show()
            } else {
                val uidAndItemID = qrContentBox.text.split(":")
                val item = runBlocking { ItemsRepository.getSingleItemFromIDs(uidAndItemID[0], uidAndItemID[1]) }
                if(item == null){
                    Snackbar.make(it,
                        "ERROR: the QR code you scanned contains badly formatted data",
                        Snackbar.LENGTH_LONG)
                        .show()
                } else {
                    runBlocking {
                        ConvRepository.addNewConversation(
                            Firebase.auth.uid!!,
                            uidAndItemID[0],
                            item.itemName, FirebaseFirestore.getInstance())
                    }
                    MyFCMMessagingService.sendNotification(this,
                        "One of your lost items has been found !",
                        "Someone has found your item '${item.itemName}'")

                }
            }
//            displayReportNotification()
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.startPreview()
    }

    override fun onPause() {
        viewModel.releaseResources()
        super.onPause()
    }

    private fun displayReportNotification() {
        MyFCMMessagingService.sendNotification(
            this, "Item found",
            "One of your Items was found !"
        )
    }

}
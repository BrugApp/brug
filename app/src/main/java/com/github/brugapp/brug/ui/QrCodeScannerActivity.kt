package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.liveData
import com.github.brugapp.brug.R
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import com.github.brugapp.brug.view_model.QrCodeScanViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

//library found on github: https://github.com/yuriy-budiyev/code-scanner
@AndroidEntryPoint
class QrCodeScannerActivity : AppCompatActivity() {

    private val viewModel: QrCodeScanViewModel by viewModels()

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseStorage: FirebaseStorage

    @Inject
    lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)
        viewModel.checkPermission(this)
        viewModel.codeScanner(this)

        //TODO: REMOVE THIS HARDCODED TEXT
        findViewById<EditText>(R.id.edit_message)

        findViewById<Button>(R.id.buttonReportItem).setOnClickListener {
            val context = this
            liveData(Dispatchers.IO){
                emit(
                    viewModel.parseTextAndCreateConv(
                        findViewById<EditText>(R.id.edit_message).text,
                        context,
                        firebaseAuth, firestore, firebaseStorage
                    )
                )
            }.observe(context){ successState ->
                if(successState){
                    Toast.makeText(context, "Thank you ! The user will be notified.", Toast.LENGTH_LONG).show()
                    val myIntent =
                        if(firebaseAuth.currentUser == null)
                            Intent(this, SignInActivity::class.java)
                        else
                            Intent(this, ChatMenuActivity::class.java)
                        startActivity(myIntent)
                } else {
                    Toast.makeText(context, "ERROR: An error has occurred, try again.", Toast.LENGTH_LONG).show()
                }
            }
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
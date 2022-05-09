package com.github.brugapp.brug.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.brugapp.brug.LOCATION_REQUEST_CODE
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ConvRepository
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.MessageRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.github.brugapp.brug.view_model.QrCodeScanViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime


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
            val activity = this
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
                requestLocationPermissions()
                var isAnonymous = false
                if(Firebase.auth.uid == null){
                    runBlocking {
                        val auth = FirebaseAuth.getInstance().signInAnonymously().await()
                        if(auth.user == null){
                            //THROW ERROR
                        } else {
                            UserRepository.addUserFromAccount(
                                auth.user!!.uid,
                                BrugSignInAccount("Anonymous", "User", "", ""),
                                FirebaseFirestore.getInstance()
                            )
                        }
                    }
                    isAnonymous = true
                }

                val uidAndItemID = qrContentBox.text.split(":")
                val item = runBlocking { ItemsRepository.getSingleItemFromIDs(uidAndItemID[0], uidAndItemID[1]) }
                val convID = "${Firebase.auth.uid!!}${uidAndItemID[0]}"
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
                            item.itemName, FirebaseFirestore.getInstance()
                        )
                    }

                    val senderName = if(isAnonymous) {
                        "Anonymous User"
                    } else {
                        "Me"
                    }

                    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
                    //TODO: REQUEST LOCATION TO SEND LOCATION MESSAGE
                    runBlocking {
                        requestLocation(
                            senderName,
                            convID,
                            Firebase.auth.uid!!,
                            fusedLocationClient,
                            locationManager,
                            FirebaseFirestore.getInstance(),
                            FirebaseAuth.getInstance(),
                            FirebaseStorage.getInstance()
                        )
                    }

                    val textMessage = TextMessage(
                        senderName,
                        DateService.fromLocalDateTime(LocalDateTime.now()),
                        "Hey ! I just found your item, I have sent you my location so that you know where it was."
                    )

                    runBlocking{
                        MessageRepository.addMessageToConv(
                            textMessage,
                            Firebase.auth.uid!!,
                            convID,
                            FirebaseFirestore.getInstance(),
                            FirebaseAuth.getInstance(),
                            FirebaseStorage.getInstance()
                        )
                    }
                    MyFCMMessagingService.sendNotification(this,
                        "One of your lost items has been found !",
                        "Someone has found your item '${item.itemName}'")

                    if(isAnonymous){
                        Firebase.auth.signOut()
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, ChatMenuActivity::class.java)
                        startActivity(intent)
                    }

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

    @SuppressLint("MissingPermission")
    private fun requestLocation(
        senderName: String,
        convID: String,
        authUID: String,
        fusedLocationClient: FusedLocationProviderClient,
        locationManager: LocationManager,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        }

        val message = Message(
            senderName,
            DateService.fromLocalDateTime(LocalDateTime.now()),
            ""
        )

        fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation: Location? ->
            if (lastKnownLocation != null) {
                runBlocking {
                    MessageRepository.addMessageToConv(
                        LocationMessage.fromMessage(message, LocationService.fromAndroidLocation(lastKnownLocation)),
                        authUID,
                        convID,
                        firestore, firebaseAuth, firebaseStorage
                    )
                }
            } else {
                // Launch the locationListener (updates every 1000 ms)
                val locationGpsProvider = LocationManager.GPS_PROVIDER
                locationManager.requestLocationUpdates(
                    locationGpsProvider,
                    50,
                    0.1f
                ) {
                    runBlocking {
                        MessageRepository.addMessageToConv(
                            LocationMessage.fromMessage(message, LocationService.fromAndroidLocation(it)),
                            authUID,
                            convID,
                            firestore, firebaseAuth, firebaseStorage
                        )
                    }
                }

                // Stop the update as we only want it once (at least for now)
                locationManager.removeUpdates {
                    runBlocking {
                        MessageRepository.addMessageToConv(
                            LocationMessage.fromMessage(message, LocationService.fromAndroidLocation(it)),
                            authUID,
                            convID,
                            firestore, firebaseAuth, firebaseStorage
                        )
                    }
                }
            }

        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), LOCATION_REQUEST_CODE
        )
    }

}
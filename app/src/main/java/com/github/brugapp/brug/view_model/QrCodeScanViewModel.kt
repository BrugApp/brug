package com.github.brugapp.brug.view_model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.text.Editable
import android.util.Log
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.budiyev.android.codescanner.*
import com.github.brugapp.brug.LOCATION_REQUEST_CODE
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ConvRepository
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.MessageRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

private const val CAMERA_REQUEST_CODE = 101

class QrCodeScanViewModel : ViewModel() {

    private lateinit var codeScanner: CodeScanner

    fun checkPermission(context: Context) {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_DENIED)
            ActivityCompat
                .requestPermissions(
                    context as Activity, arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
    }

    fun codeScanner(activity: Activity) {
        val scannerView = activity.findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(activity.applicationContext, scannerView)
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false
            // HERE LIES THE CODE HANDLING WHAT HAPPENS AFTER SCANNING THE QR CODE
            decodeCallback = DecodeCallback {
                activity.runOnUiThread {
                    activity.findViewById<EditText>(R.id.editTextReportItem).setText(it.text)
                }
            }
            errorCallback = ErrorCallback {
                activity.runOnUiThread {
                    Log.e("", "Camera initialization error: ${it.message}")
                }
            }
        }
    }

    suspend fun parseTextAndCreateConv(qrText: Editable,
                            context: Activity,
                            firebaseAuth: FirebaseAuth,
                            firestore: FirebaseFirestore,
                            firebaseStorage: FirebaseStorage): Boolean {

        if(qrText.isBlank() || !qrText.contains(":")){
            return false
        } else {
            val isAnonymous = firebaseAuth.currentUser == null
            val convID = createNewConversation(isAnonymous, qrText, firebaseAuth, firestore)
            return if (convID == null) {
                if(isAnonymous) firebaseAuth.signOut()
                false
            } else {
                val hasSentMessages = sendMessages(
                    firebaseAuth.currentUser!!.uid,
                    convID,
                    isAnonymous,
                    context,
                    firestore, firebaseAuth, firebaseStorage
                )

                if(isAnonymous) firebaseAuth.signOut()
                hasSentMessages
            }

        }
    }

    private suspend fun createNewConversation(isAnonymous: Boolean,
                                              qrText: Editable,
                                              firebaseAuth: FirebaseAuth,
                                              firestore: FirebaseFirestore): String? {
        if(isAnonymous){
            val auth = firebaseAuth.signInAnonymously().await().user ?: return null
            UserRepository.addUserFromAccount(
                auth.uid,
                BrugSignInAccount("Anonymous","User","",""),
                false,
                firestore)
        }

        val (userID, itemID) = qrText.toString().split(":")

        val lostItemName = getItemNameForNewConversation(userID, itemID) ?: return null
//        val convID = firebaseAuth.currentUser!!.uid + itemID
        val response = ConvRepository.addNewConversation(
            firebaseAuth.currentUser!!.uid,
            userID,
            lostItemName,
            firestore
        )

        return if(response.onSuccess) firebaseAuth.currentUser!!.uid + userID else null
    }

    private suspend fun sendMessages(senderID: String,
                                     convID: String,
                                     isAnonymous: Boolean,
                                     context: Activity,
                                     firestore: FirebaseFirestore,
                                     firebaseAuth: FirebaseAuth,
                                     firebaseStorage: FirebaseStorage): Boolean {
        val senderName = if(isAnonymous) "Anonymous User" else "Me"

        // Getting the location and sending the message
        requestLocationPermissions(context)
        getLocationAndSendMessage(
            senderName,
            convID,
            firebaseAuth.currentUser!!.uid,
            context,
            firestore,
            firebaseAuth,
            firebaseStorage
        )

        // Creating and sending the text message
        val textMessage = TextMessage(
            senderName,
            DateService.fromLocalDateTime(LocalDateTime.now()),
            "Hey ! I just found your item, I have sent you my location so that you know where it was."
        )

        return MessageRepository.addMessageToConv(
            textMessage,
            senderID,
            convID,
            firestore,
            firebaseAuth,
            firebaseStorage
        ).onSuccess
    }

    private suspend fun getItemNameForNewConversation(userID: String, itemID: String): String? {
        return ItemsRepository.getSingleItemFromIDs(userID, itemID)?.itemName
    }


    @SuppressLint("MissingPermission")
    fun getLocationAndSendMessage(
        senderName: String,
        convID: String,
        authUID: String,
        context: Activity,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (checkLocationPermissions(context)) {
            requestLocationPermissions(context)
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation: Location? ->
            if (lastKnownLocation != null) {
                sendLocationMessage(senderName, lastKnownLocation, convID, authUID, firestore, firebaseAuth, firebaseStorage)
            } else {
                // Launch the locationListener (updates every 1000 ms)
                val locationGpsProvider = LocationManager.GPS_PROVIDER
                locationManager.requestLocationUpdates(
                    locationGpsProvider, 50, 0.1f
                ) {
                    sendLocationMessage(senderName, it, convID, authUID, firestore, firebaseAuth, firebaseStorage)
                }

                // Stop the update as we only want it once (at least for now)
                locationManager.removeUpdates {
                    sendLocationMessage(senderName, it, convID, authUID, firestore, firebaseAuth, firebaseStorage)
                }
            }
        }
    }

    private fun sendLocationMessage(senderName: String,
                                    location: Location,
                                    convID: String,
                                    authUID: String,
                                    firestore: FirebaseFirestore,
                                    firebaseAuth: FirebaseAuth,
                                    firebaseStorage: FirebaseStorage){
        val locationMessage = LocationMessage(
                senderName,
                DateService.fromLocalDateTime(LocalDateTime.now()),
                "",
                LocationService.fromAndroidLocation(location)
            )

        runBlocking {
            MessageRepository.addMessageToConv(
                locationMessage,
                authUID,
                convID,
                firestore, firebaseAuth, firebaseStorage
            )
        }
    }

    private fun requestLocationPermissions(context: Activity) {
        ActivityCompat.requestPermissions(
            context, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), LOCATION_REQUEST_CODE
        )
    }

    private fun checkLocationPermissions(context: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

    }

    fun startPreview() {
        codeScanner.startPreview()
    }

    fun releaseResources() {
        codeScanner.releaseResources()
    }


}
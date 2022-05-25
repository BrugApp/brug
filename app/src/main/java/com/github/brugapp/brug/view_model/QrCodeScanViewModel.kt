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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budiyev.android.codescanner.*
import com.github.brugapp.brug.LOCATION_REQUEST_CODE
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ConvRepository
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.MessageRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

class QrCodeScanViewModel : ViewModel() {

    private lateinit var codeScanner: CodeScanner

    fun checkPermissions(context: Context) {
        val permissionRequestCode = 1
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if(!hasPermissions(context, permissions)){
            ActivityCompat.requestPermissions(context as Activity, permissions, permissionRequestCode)
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
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
                    activity.findViewById<EditText>(R.id.edit_message).setText(it.text)
                }
            }
            errorCallback = ErrorCallback {
                activity.runOnUiThread {
                    Log.e("", "Camera initialization error: ${it.message}")
                }
            }
        }
    }

    suspend fun parseTextAndCreateConv(qrText: String,
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
                val senderName = if(isAnonymous) "Anonymous User" else "Me"
                getLocationAndNotifyUser(
                    senderName,
                    convID,
                    firebaseAuth.uid!!,
                    context,
                    qrText,
                    firestore,
                    firebaseAuth,
                    firebaseStorage
                )
                if(isAnonymous) firebaseAuth.signOut()
                true
            }
        }
    }

    private suspend fun createNewConversation(isAnonymous: Boolean,
                                              qrText: String,
                                              firebaseAuth: FirebaseAuth,
                                              firestore: FirebaseFirestore): String? {
        if (isAnonymous) {
            val auth = firebaseAuth.signInAnonymously().await().user ?: return null
            UserRepository.addUserFromAccount(
                auth.uid,
                BrugSignInAccount("Anonymous", "User", "", ""),
                false,
                firestore
            )
        }


        val userID = qrText.split(":")[0]

        val response = ConvRepository.addNewConversation(
            firebaseAuth.currentUser!!.uid,
            userID,
            qrText,
            null,
            firestore
        )

        return if (response.onSuccess) firebaseAuth.currentUser!!.uid + userID else null
    }

    @SuppressLint("MissingPermission")
    fun getLocationAndNotifyUser(
        senderName: String,
        convID: String,
        authUID: String,
        context: Activity,
        qrText: String,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (isLocationPermissionsDenied(context)) {
            requestLocationPermissions(context)
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation: Location? ->
            if (lastKnownLocation != null) {
                sendMessages(senderName, lastKnownLocation, convID, authUID, firestore, firebaseAuth, firebaseStorage)
                viewModelScope.launch { setItemLastLocation(qrText, lastKnownLocation, firestore) }

            } else {
                // Launch the locationListener (updates every 1000 ms)
                val locationGpsProvider = LocationManager.GPS_PROVIDER
                locationManager.requestLocationUpdates(
                    locationGpsProvider, 50, 0.1f
                ) {
                    sendMessages(senderName, it, convID, authUID, firestore, firebaseAuth, firebaseStorage)
                    viewModelScope.launch { setItemLastLocation(qrText, it, firestore) }
                }

                // Stop the update as we only want it once (at least for now)
                locationManager.removeUpdates {
                    sendMessages(senderName, it, convID, authUID, firestore, firebaseAuth, firebaseStorage)
                    viewModelScope.launch { setItemLastLocation(qrText, it, firestore) }
                }
            }
        }
    }

    private fun sendMessages(
        senderName: String,
        location: Location,
        convID: String,
        authUID: String,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage) {

        listOf(
            LocationMessage(
                senderName,
                DateService.fromLocalDateTime(LocalDateTime.now()),
                "📍 Location",
                LocationService.fromAndroidLocation(location)
            ),

            TextMessage(
                senderName,
                DateService.fromLocalDateTime(LocalDateTime.now()),
                "Hey ! I just found your item, I have sent you my location so that you know where it was."
            )
        ).map { message ->
            viewModelScope.launch {
                MessageRepository.addMessageToConv(
                    message,
                    authUID,
                    convID,
                    firestore,
                    firebaseAuth,
                    firebaseStorage
                )
            }
        }
    }

    private suspend fun setItemLastLocation(
        qrStr: String,
        location: Location,
        firestore: FirebaseFirestore
    ): Boolean{
        val (userID, itemID) = qrStr.split(":")
        return ItemsRepository.addLastLocation(
            userID,
            itemID,
            LocationService.fromAndroidLocation(location),
            firestore
        ).onSuccess
    }

    private fun requestLocationPermissions(context: Activity) {
        ActivityCompat.requestPermissions(
            context, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), LOCATION_REQUEST_CODE
        )
    }

    private fun isLocationPermissionsDenied(context: Activity): Boolean {
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
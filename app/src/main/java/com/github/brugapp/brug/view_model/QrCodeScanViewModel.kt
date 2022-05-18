package com.github.brugapp.brug.view_model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.text.Editable
import android.util.Log
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.budiyev.android.codescanner.*
import com.github.brugapp.brug.LOCATION_REQUEST_CODE
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.*
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
                val hasSentMessages = sendMessagesAndUpdateLastLocation(
                    qrText.toString(),
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
                firestore
            )
        }

        val userID = qrText.toString().split(":")[0]
        val response = ConvRepository.addNewConversation(
            firebaseAuth.currentUser!!.uid,
            userID,
            qrText.toString(),
            null,
            firestore
        )

        return if(response.onSuccess) firebaseAuth.currentUser!!.uid + userID else null
    }

    private suspend fun sendMessagesAndUpdateLastLocation(
        itemID: String,
        convID: String,
        isAnonymous: Boolean,
        context: Activity,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): Boolean {
        val senderName = if (isAnonymous) "Anonymous User" else firebaseAuth.currentUser!!.displayName!!

        // Getting the location
        val lastLocation = getLastLocation(context) ?: return false

        // Sending the messages
        val messagesResponse = sendMessages(
            senderName,
            lastLocation,
            convID,
            firebaseAuth.uid!!,
            firestore,
            firebaseAuth,
            firebaseStorage
        )
        if (!messagesResponse) return false

        // Updating the last item location
        val (userID, associatedItemID) = itemID.split(":")
        return ItemsRepository.addLastLocation(
            userID,
            associatedItemID,
            lastLocation,
            firestore
        ).onSuccess
    }


    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(context: Activity): LocationService? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (checkLocationPermissions(context)) {
            requestLocationPermissions(context)
        }

        return try {
            LocationService.fromAndroidLocation(fusedLocationClient.lastLocation.await())
        } catch (e: Exception) {
            Log.e("LOCATION ERROR", "Unable to fetch the last location")
            null
        }
    }

    private suspend fun sendMessages(
        senderName: String,
        location: LocationService,
        convID: String,
        authUID: String,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage): Boolean{

        val textMessage = TextMessage(
            senderName,
            DateService.fromLocalDateTime(LocalDateTime.now()),
            "Hey ! I just found your item, I have sent you my location so that you know where it was."
        )

        val response = MessageRepository.addMessageToConv(
            textMessage, senderName, authUID, convID,
            firestore, firebaseAuth, firebaseStorage
        ).onSuccess

        if(response){
            val locationMessage = LocationMessage(
                    senderName,
                    DateService.fromLocalDateTime(LocalDateTime.now()),
                "📍 Location",
                    location
            )

            return MessageRepository.addMessageToConv(
                locationMessage, senderName, authUID, convID,
                firestore, firebaseAuth, firebaseStorage
            ).onSuccess
        }

        return response
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
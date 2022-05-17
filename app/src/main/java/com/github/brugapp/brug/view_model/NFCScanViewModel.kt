package com.github.brugapp.brug.view_model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.nfc.*
import android.nfc.NfcAdapter.ACTION_TAG_DISCOVERED
import android.nfc.tech.Ndef
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.LOCATION_REQUEST_CODE
import com.github.brugapp.brug.data.ConvRepository
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.MessageRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.time.LocalDateTime
import kotlin.experimental.and

private const val NFC_REQUEST_CODE = 1000101

class NFCScanViewModel : ViewModel() {
    /**
     * @param context1
     */
    fun checkNFCPermission(context1: Context){ if(PackageManager.PERMISSION_DENIED == checkSelfPermission(context1,Manifest.permission.NFC)) requestPermissions(context1 as Activity, arrayOf(Manifest.permission.NFC), NFC_REQUEST_CODE) }

    /**
     * @param this1
     * @return
     */
    fun setupAdapter(this1: Context): NfcAdapter? { return try{ NfcAdapter.getDefaultAdapter(this1) }catch(e: Exception){ null } }

    /**
     * @param this1
     * @return Pair<PendingIntent,Array<IntentFilter>>
     */
    fun setupWritingTagFilters(this1: Context): Pair<PendingIntent,Array<IntentFilter>>{ return Pair(PendingIntent.getActivity(this1, 0, Intent(this1, this1.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), FLAG_MUTABLE),arrayOf(setupTag())) }

    /**
     * @use prepares the IntentFilter needed to produce the 2nd Pair element of setupWritingTagFilters
     * @return IntentFilter with an added default category
     */
    fun setupTag(): IntentFilter {
        val tagDetected = IntentFilter(ACTION_TAG_DISCOVERED)
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT)
        return tagDetected
    }

    /**
     * @param nfcContents
     * @param intent
     */
    fun readFromIntent(nfcContents: TextView, intent: Intent){ if (checkIntentAction(intent)&&rawMessageToMessage(intent).first){ buildTagViews(nfcContents, rawMessageToMessage(intent).second) } }

    /**
     * @use combines 3 condition checks into a single boolean for calling readFromIntent
     * @param intent used to get the associated action for testing if tag is discovered
     * @return true when the intent action corresponds to a tag being discovered false otherwise
     */
    fun checkIntentAction(intent: Intent): Boolean {
        val isACTIONTAGDISCOVERED = intent.action.equals(ACTION_TAG_DISCOVERED)
        val isACTIONTECHDISCOVERED = intent.action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)
        val isACTIONNDEFDISCOVERED = intent.action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)
        return (isACTIONTAGDISCOVERED || isACTIONTECHDISCOVERED || isACTIONNDEFDISCOVERED)
    }

    /**
     * @use sets up the data and conditions needed for calling buildTagViews
     * @param intent used for getting extended data from this intent
     * @return Boolean: this object checks if the extended data from intent is null
     * @return Array<NdefMessage>: this object represented the extended data from intent
     */
    fun rawMessageToMessage(intent: Intent): Pair<Boolean,Array<NdefMessage>> {

        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        var messages: Array<NdefMessage> = arrayOf()
        var bool = false

        if (rawMessages!=null) {
            messages = Array(rawMessages.size) { i -> rawMessages[i] as NdefMessage }
            bool = true
        }
        return Pair(bool,messages)
    }

    /**
     * @use converts NdefMessage Array into a String message understandable to humans
     * @param nfcContents
     * @param messages is an array of NdefMessages, a lightweight binary format for tags
     * @return String computed from the tag's NdefMessage Array
     */
    @SuppressLint("SetTextI18n")
    fun buildTagViews(nfcContents: TextView, messages: Array<NdefMessage>){ try{ nfcContents.text = "Read Tag Contents:" + initText(messages) }catch (e : UnsupportedEncodingException){ Log.e("UnsupportedEncoding",e.toString()) } }

    @Throws(UnsupportedEncodingException::class)
    fun initText(messages: Array<NdefMessage>?): String {
        val bugText : String = "null message error"
        var text: String = ""
        return when {
            messages==null -> {
                bugText
            }
            messages.isEmpty() -> {
                text
            }
            else -> {
                val payload = messages[0].records[0].payload
                val textEncoding =
                    if ((payload[0] and 128.toByte()).toInt() == 0) Charset.forName("UTF-8")
                    else Charset.forName("UTF-16")
                val languageCodeLength = payload[0] and 63.toByte()
                text = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, textEncoding)
                text
            }
        }
    }

    /**
     * @param text
     * @param tag
     */
    @Throws(IOException::class, FormatException::class)
    fun write(text: String, tag: Tag) { Ndef.get(tag).connect()
        Ndef.get(tag).writeNdefMessage(NdefMessage(arrayOf(createRecord(text))))
        Ndef.get(tag).close() }

    /** the write function calls createRecord in order to write a given message to the tag
     * @param text the string message we want to build the record from
     * @return an immutable NdefRecord
     */
    @Throws(UnsupportedEncodingException::class)
    fun createRecord(text: String): NdefRecord {
        val lang = "en"
        //we can change the record language in the previous line
        val textBytes = text.toByteArray()
        val langBytes = lang.toByteArray()
        val langLength = langBytes.size
        val textLength = textBytes.size
        var payload: ByteArray = ByteArray(langLength+textLength+1)
        payload[0] = langLength.toByte()
        System.arraycopy(langBytes, 0, payload, 1, langLength)
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength)
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), payload)

    }

    /**
     * @use notify owner that their item is found
     * @param this1
     */
    fun displayReportNotification(this1: Context) { MyFCMMessagingService.sendNotification(this1, "Item found", "One of your items was found!") }

    suspend fun parseTextAndCreateConv(nfcText: String, context: Activity, firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore, firebaseStorage: FirebaseStorage): Boolean {

        return if(nfcText.isBlank() || !nfcText.contains(":")){
            false
        } else {
            val isAnonymous = firebaseAuth.currentUser == null
            val convID = createNewConversation(isAnonymous, nfcText, firebaseAuth, firestore)
            if (convID == null) {
                if(isAnonymous) firebaseAuth.signOut()
                false
            } else {
                val hasSentMessages = sendMessages(firebaseAuth.currentUser!!.uid, convID, isAnonymous, context, firestore, firebaseAuth, firebaseStorage)
                if(isAnonymous) firebaseAuth.signOut()
                hasSentMessages
            }
        }
    }

    /**
     * @use if item exists we notify owner, else we add item to user's item collection
     * @param isAnonymous
     * @param nfcText
     * @param firebaseAuth
     * @param firestore
     * @return
     */
    private suspend fun createNewConversation(isAnonymous: Boolean, nfcText: String, firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore): String? {
        if(isAnonymous){
            val auth = firebaseAuth.signInAnonymously().await().user ?: return null
            UserRepository.addUserFromAccount(auth.uid, BrugSignInAccount("Anonymous","User","",""), false, firestore)
        }
        val (userID, itemID) = nfcText.split(":")
        val item = ItemsRepository.getSingleItemFromIDs(userID, itemID)
        return if(item == null){
            ItemsRepository.addItemWithItemID(MyItem("default item name",itemID.toInt(),"default description",false),itemID,userID,firestore)
            firebaseAuth.currentUser!!.uid + userID
        }else {
            val response = ConvRepository.addNewConversation(firebaseAuth.currentUser!!.uid, userID, "$userID:$itemID", firestore)
            if(response.onSuccess) firebaseAuth.currentUser!!.uid + userID else null
        }
    }

    /**
     * @param senderID
     * @param convID
     * @param isAnonymous
     * @param context
     * @param firestore
     * @param firebaseAuth
     * @param firebaseStorage
     * @return
     */
    private suspend fun sendMessages(senderID: String, convID: String, isAnonymous: Boolean, context: Activity, firestore: FirebaseFirestore, firebaseAuth: FirebaseAuth, firebaseStorage: FirebaseStorage): Boolean {
        val senderName = if(isAnonymous) "Anonymous User" else "Me"
        // Getting the location and sending the message
        requestLocationPermissions(context)
        getLocationAndSendMessage(senderName, convID, firebaseAuth.currentUser!!.uid, context, firestore, firebaseAuth, firebaseStorage)
        // Creating and sending the text message
        val textMessage = TextMessage(senderName, DateService.fromLocalDateTime(LocalDateTime.now()), "Hey ! I just found your item, I have sent you my location so that you know where it was.")
        return MessageRepository.addMessageToConv(textMessage, senderID, convID, firestore, firebaseAuth, firebaseStorage).onSuccess
    }

    /**
     * @param context
     */
    private fun requestLocationPermissions(context: Activity) {
        requestPermissions(context, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
    }

    /**
     * @param senderName
     * @param convID
     * @param authUID
     * @param context
     * @param firestore
     * @param firebaseAuth
     * @param firebaseStorage
     */
    @SuppressLint("MissingPermission")
    fun getLocationAndSendMessage(senderName: String, convID: String, authUID: String, context: Activity, firestore: FirebaseFirestore, firebaseAuth: FirebaseAuth, firebaseStorage: FirebaseStorage) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (checkLocationPermissions(context)) requestLocationPermissions(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation: Location? ->
            if (lastKnownLocation != null) {
                sendLocationMessage(senderName, lastKnownLocation, convID, authUID, firestore, firebaseAuth, firebaseStorage)
            } else { // Launch the locationListener (updates every 1000 ms)
                val locationGpsProvider = LocationManager.GPS_PROVIDER
                locationManager.requestLocationUpdates(locationGpsProvider, 50, 0.1f) {
                    sendLocationMessage(senderName, it, convID, authUID, firestore, firebaseAuth, firebaseStorage)
                }
                // Stop the update as we only want it once (at least for now)
                locationManager.removeUpdates {
                    sendLocationMessage(senderName, it, convID, authUID, firestore, firebaseAuth, firebaseStorage) } } }
    }

    /**
     * @param context
     * @return
     */
    private fun checkLocationPermissions(context: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    }

    /**
     * @param senderName
     * @param location
     * @param convID
     * @param authUID
     * @param firestore
     * @param firebaseAuth
     * @param firebaseStorage
     */
    private fun sendLocationMessage(senderName: String, location: Location, convID: String, authUID: String, firestore: FirebaseFirestore, firebaseAuth: FirebaseAuth, firebaseStorage: FirebaseStorage){
        val locationMessage = LocationMessage(senderName, DateService.fromLocalDateTime(LocalDateTime.now()), "", LocationService.fromAndroidLocation(location))
        runBlocking { MessageRepository.addMessageToConv(locationMessage,authUID, convID, firestore, firebaseAuth, firebaseStorage) } }
}
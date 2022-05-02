package com.github.brugapp.brug.view_model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.devlomi.record_view.RecordButton
import com.github.brugapp.brug.*
import com.github.brugapp.brug.data.MessageRepository
import com.github.brugapp.brug.model.ChatMessagesListAdapter
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.github.brugapp.brug.ui.ChatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.net.URI
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

//TODO: NEEDS REFACTORING & DOCUMENTATION
class ChatViewModel() : ViewModel() {
    private lateinit var adapter: ChatMessagesListAdapter
    private lateinit var mediaRecorder : MediaRecorder
    private lateinit var mediaPlayer : MediaPlayer
    private lateinit var audioPath : String
    private lateinit var imageUri: Uri // NEEDED TO RETRIEVE THE IMAGE FROM THE CAMERA AFTER SNAPPING A PICTURE

    //private val locationListener = LocationListener { sendLocation(it) }
    private lateinit var activity: ChatActivity

    //TODO: Remove initial init. and revert to lateinit var
    private var messages: MutableList<Message> = mutableListOf()

    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH)

    fun initViewModel(messages: MutableList<Message>, activity: ChatActivity) {
        this.messages = messages

        // initiate arguments values for location messages
        for (message in messages){
            if(message is LocationMessage){
                message.mapUrl = activity.createFakeImage().toString()
            }
        }

        this.adapter = ChatMessagesListAdapter(this, messages)
    }

    fun getAdapter(): ChatMessagesListAdapter {
        return adapter
    }

    fun sendMessage(message: Message, convID: String, activity: ChatActivity) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        activity.scrollToBottom(adapter.itemCount - 1)

        if(Firebase.auth.currentUser == null){
            Snackbar.make(activity.findViewById(android.R.id.content),
                "ERROR: You are no longer logged in ! Log in again to send the message.",
                Snackbar.LENGTH_LONG)
                .show()
        } else {
            liveData(Dispatchers.IO) {
                emit(MessageRepository.addMessageToConv(message, Firebase.auth.currentUser!!.uid, convID))
            }.observe(activity) { response ->
                if(response.onError != null){
                    Log.e("FIREBASE ERROR", response.onError!!.message.toString())
                    Snackbar.make(activity.findViewById(android.R.id.content),
                        "ERROR: Unable to register the new message in the database",
                        Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    // LOCATION RELATED
    fun requestLocation(
        activity: Activity,
        fusedLocationClient: FusedLocationProviderClient,
        locationManager: LocationManager
    ) {
        this.activity = activity as ChatActivity
        if (ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                LOCATION_REQUEST_CODE
            )
        }
    }

    private fun sendLocationMessage(activity: ChatActivity, location: Location, convID: String){
        val textBox = activity.findViewById<TextView>(R.id.editMessage)
        val strMsg = textBox.text.toString().ifBlank { "📍 Location" }
        val uri = activity.createFakeImage()
        val newMessage = LocationMessage(
            "Me",
            DateService.fromLocalDateTime(LocalDateTime.now()),
            strMsg,
            LocationService.fromAndroidLocation(location)
        )

        newMessage.mapUrl = activity.createFakeImage().toString()
        sendMessage(newMessage, convID, activity)

        // Clear the message field
        textBox.text = ""

        adapter.notifyItemInserted(messages.size - 1)
    }

    fun sendPicMessage(activity: ChatActivity, convID: String) {
        val textBox = activity.findViewById<TextView>(R.id.editMessage)
        val strMsg = textBox.text.toString().ifBlank { "📷 Image" }
        //val resizedUri = resize(activity, imageUri) //still useful??
        val newMessage = PicMessage(
            "Me",
            DateService.fromLocalDateTime(LocalDateTime.now()),
            strMsg,
            compressImage(activity, imageUri).toString()
        )

        sendMessage(newMessage, convID, activity)

        // Clear the message field
        textBox.text = ""

        activity.scrollToBottom(adapter.itemCount - 1)
    }

    // IMAGE RELATED =======================================================
    fun takeCameraImage(activity: ChatActivity) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null) {
            val imageFile = createImageFile(activity)
            // Create a file Uri for saving the image
            imageUri = FileProvider.getUriForFile(
                activity,
                "com.github.brugapp.brug.fileprovider",
                imageFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(activity, intent, TAKE_PICTURE_REQUEST_CODE, null)
        }
    }

    fun setImageUri(uri: Uri){
        imageUri = uri
    }

    fun selectGalleryImage(activity: ChatActivity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(activity, intent, SELECT_PICTURE_REQUEST_CODE, null)
    }

    // Create a File for saving the image (and the name)
    private fun createImageFile(cont: Context): File {
        val storageDir: File? = cont.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_",
            ".jpg",
            storageDir
        )
    }

    private fun uriToBitmap(activity: ChatActivity, selectedFileUri: Uri): Bitmap {
        val parcelFileDescriptor = activity.contentResolver.openFileDescriptor(selectedFileUri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun compressImage(activity: ChatActivity, uri: Uri): URI {
        // open the image and resize it
        val imageBM = uriToBitmap(activity, uri)
        val resized = Bitmap.createScaledBitmap(imageBM, 500, 500, false)

        // store to new file
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val outputFile = createImageFile(activity)
        outputFile.writeBytes(outputStream.toByteArray())
        outputStream.flush()
        outputStream.close()

        // return uri of new file
        return outputFile.toURI()
    }

    @SuppressLint("MissingPermission")
    fun requestLocation(
        convID: String,
        activity: ChatActivity,
        fusedLocationClient: FusedLocationProviderClient,
        locationManager: LocationManager
    ) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions(activity)
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation: Location? ->
            if (lastKnownLocation != null) {
                sendLocationMessage(activity, lastKnownLocation, convID)
            } else {
                // Launch the locationListener (updates every 1000 ms)
                val locationGpsProvider = LocationManager.GPS_PROVIDER
                locationManager.requestLocationUpdates(
                    locationGpsProvider,
                    50,
                    0.1f
                ) { sendLocationMessage(activity, it, convID) }

                // Stop the update as we only want it once (at least for now)
                locationManager.removeUpdates { sendLocationMessage(activity, it, convID) }
            }
        }
    }

    fun setupRecording(){

        audioPath = Environment.getExternalStorageDirectory().absolutePath + "/Documents/audio.3gp"

        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            mediaRecorder.setOutputFile(audioPath)
            mediaRecorder.prepare()
            mediaRecorder.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setListenForRecord(recordButton : RecordButton, bool : Boolean){
        recordButton.isListenForRecord = bool
    }

    fun deleteAudio(){
        mediaRecorder.reset()
        mediaRecorder.release()
        val file = File(audioPath)
        if(file.exists()){
            file.delete()
        }
    }

    fun stopAudio() {
        mediaRecorder.stop()
        mediaRecorder.release()
    }

    fun sendAudio(){

        // TODO: modify this implementation to adapt it for Firestore
        val audioMessage = AudioMessage("Me", DateService.fromLocalDateTime(LocalDateTime.now()),
            "Audio", audioPath)

        messages.add(audioMessage)
        adapter.notifyItemInserted(messages.size - 1)
    }

    // PERMISSIONS RELATED =======================================================

    fun requestRecording(activity: Activity) {
        requestPermissions(
            activity,
            Array(1) { Manifest.permission.RECORD_AUDIO },
            RECORDING_REQUEST_CODE
        )
    }

    fun isExtStorageOk(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isAudioPermissionOk(context : Context) : Boolean{
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    fun requestExtStorage(activity: Activity){
        requestPermissions(activity, Array(1){Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE)
    }

    private fun requestLocationPermissions(activity: Activity) {
        requestPermissions(
            activity, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), LOCATION_REQUEST_CODE
        )
    }
}
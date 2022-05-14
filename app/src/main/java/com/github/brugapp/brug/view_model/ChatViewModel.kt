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
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import java.nio.channels.Channels
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

//TODO: NEEDS DOCUMENTATION
class ChatViewModel : ViewModel() {
    private lateinit var adapter: ChatMessagesListAdapter
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioPath: String

    // Uri needed to retrieve image from camera after taking a picture
    private lateinit var imageUri: Uri

    //TODO: Remove initial init. and revert to lateinit var
    private var messages: MutableList<Message> = mutableListOf()

    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH)

    fun initViewModel(messages: MutableList<Message>, activity: ChatActivity) {
        this.messages = messages

        // initiate arguments values for location messages
        for (message in messages) {
            if (message is LocationMessage) {
                val url = getUrlForLocation(activity, message.location.toAndroidLocation())
                runBlocking {
                    message.mapUrl = loadImageFromUrl(activity, url).toString()
                }
            }
        }

        this.adapter = ChatMessagesListAdapter(this, messages)
    }

    private fun getUrlForLocation(activity: ChatActivity, location: Location): URL {
        val lat = location.latitude.toString()
        val lon = location.longitude.toString()
        val baseUrl =
            "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/geojson(%7B%22type%22%3A%22Point%22%2C%22coordinates%22%3A%5B$lon%2C$lat%5D%7D)/"
        val posUrl = "$lon,$lat"
        val endUrl =
            ",15/500x500?logo=false&attribution=false&access_token=" + activity.getString(R.string.mapbox_access_token)
        return URL(baseUrl + posUrl + endUrl)
    }

    fun getAdapter(): ChatMessagesListAdapter {
        return adapter
    }

    fun sendMessage(
        message: Message,
        convID: String,
        activity: ChatActivity,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        activity.scrollToBottom(adapter.itemCount - 1)

        if (firebaseAuth.currentUser == null) {
            Snackbar.make(
                activity.findViewById(android.R.id.content),
                "ERROR: You are no longer logged in ! Log in again to send the message.",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            viewModelScope.launch {
                val response = MessageRepository.addMessageToConv(
                    message,
                    firebaseAuth.currentUser!!.displayName!!,
                    firebaseAuth.uid!!,
                    convID,
                    firestore,
                    firebaseAuth,
                    firebaseStorage
                )

                if(response.onError != null){
                    Log.e("FIREBASE ERROR", response.onError!!.message.toString())
                    Snackbar.make(
                        activity.findViewById(android.R.id.content),
                        "ERROR: Unable to register the new message in the database",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // LOCATION RELATED
    private fun sendLocationMessage(
        activity: ChatActivity,
        location: Location,
        convID: String,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {
        val textBox = activity.findViewById<TextView>(R.id.editMessage)
        val strMsg = textBox.text.toString().ifBlank { "📍 Location" }
        val newMessage = LocationMessage(
            "Me",
            DateService.fromLocalDateTime(LocalDateTime.now()),
            strMsg,
            LocationService.fromAndroidLocation(location)
        )

        // Get image from API or create stub one
        val url = getUrlForLocation(activity, location)
        newMessage.mapUrl = runBlocking { loadImageFromUrl(activity, url).toString() }
        sendMessage(newMessage, convID, activity, firestore, firebaseAuth, firebaseStorage)

        // Clear the message field
        textBox.text = ""

        adapter.notifyItemInserted(messages.size - 1)
    }

    private suspend fun loadImageFromUrl(activity: ChatActivity, url: URL): Uri {
        try {
            val image = createImageFile(activity)
            viewModelScope.launch(Dispatchers.IO) {
                url.openStream().use {
                    Channels.newChannel(it).use { rbc ->
                        FileOutputStream(image).use { fos ->
                            fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
                        }
                    }
                }
            }.join()
            return Uri.fromFile(image)
        } catch (e: Exception) {
            println("Error when loading image from URL: $e")
            return createFakeImage(activity)!!
        }
    }

    fun sendPicMessage(
        activity: ChatActivity,
        convID: String,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {
        val textBox = activity.findViewById<TextView>(R.id.editMessage)
        val strMsg = textBox.text.toString().ifBlank { "📷 Image" }
        //val resizedUri = resize(activity, imageUri) //still useful??
        val newMessage = PicMessage(
            "Me",
            DateService.fromLocalDateTime(LocalDateTime.now()),
            strMsg,
            compressImage(activity, imageUri).toString()
        )

        sendMessage(newMessage, convID, activity, firestore, firebaseAuth, firebaseStorage)

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

    fun setImageUri(uri: Uri) {
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
        val imageBM = uriToBitmap(activity, uri)
        val resized = Bitmap.createScaledBitmap(imageBM, 500, 500, false)
        return storeBitmap(activity, resized)
    }

    private fun storeBitmap(activity: ChatActivity, bitmap: Bitmap): URI {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val outputFile = createImageFile(activity)
        outputFile.writeBytes(outputStream.toByteArray())
        outputStream.flush()
        outputStream.close()

        return outputFile.toURI()
    }

    @SuppressLint("MissingPermission")
    fun requestLocation(
        convID: String,
        activity: ChatActivity,
        fusedLocationClient: FusedLocationProviderClient,
        locationManager: LocationManager,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
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
                sendLocationMessage(
                    activity,
                    lastKnownLocation,
                    convID,
                    firestore,
                    firebaseAuth,
                    firebaseStorage
                )
            } else {
                // Launch the locationListener (updates every 1000 ms)
                val locationGpsProvider = LocationManager.GPS_PROVIDER
                locationManager.requestLocationUpdates(
                    locationGpsProvider,
                    50,
                    0.1f
                ) {
                    sendLocationMessage(
                        activity,
                        it,
                        convID,
                        firestore,
                        firebaseAuth,
                        firebaseStorage
                    )
                }

                // Stop the update as we only want it once (at least for now)
                locationManager.removeUpdates {
                    sendLocationMessage(
                        activity,
                        it,
                        convID,
                        firestore,
                        firebaseAuth,
                        firebaseStorage
                    )
                }
            }
        }
    }

    fun setupRecording(activity: ChatActivity) {

        audioPath = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!.absolutePath +
                "/audio" + DateService.fromLocalDateTime(LocalDateTime.now()) + ".3gp"

        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            mediaRecorder.setOutputFile(Uri.parse(audioPath).toString())
            mediaRecorder.prepare()
            mediaRecorder.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun deleteAudio() {

        mediaRecorder.reset()
        mediaRecorder.release()
        val file = File(audioPath)
        if (file.exists()) {
            file.delete()
        }
    }

    fun stopAudio() {
        mediaRecorder.stop()
        mediaRecorder.release()
    }

    fun sendAudio(
        activity: ChatActivity, convID: String,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {

        val audioMessage = AudioMessage(
            "Me", DateService.fromLocalDateTime(LocalDateTime.now()),
            "Audio", Uri.fromFile(File(audioPath)).toString(), audioPath
        )
        sendMessage(audioMessage, convID, activity, firestore, firebaseAuth, firebaseStorage)
    }

    // HELPERS METHODS =======================================================
    @SuppressLint("NewApi")
    fun createFakeImage(activity: ChatActivity): Uri? {
        val encodedImage =
            "iVBORw0KGgoAAAANSUhEUgAAAKQAAACZCAYAAAChUZEyAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAG0SURBVHhe7dIxAcAgEMDALx4rqKKqDxZEZLhbYiDP+/17IGLdQoIhSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSEJmDnORA7zZz2YFAAAAAElFTkSuQmCC"
        val decodedImage = Base64.getDecoder().decode(encodedImage)
        val image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)

        // store to outputstream
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        // create file
        val imageFile = createImageFile(activity)

        // get URI for file
        val uri = FileProvider.getUriForFile(
            activity,
            "com.github.brugapp.brug.fileprovider",
            imageFile
        )

        // store file
        storeBitmap(activity, image)
        return uri
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

    fun isAudioPermissionOk(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestExtStorage(activity: Activity) {
        requestPermissions(
            activity,
            Array(1) { Manifest.permission.WRITE_EXTERNAL_STORAGE },
            STORAGE_REQUEST_CODE
        )
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

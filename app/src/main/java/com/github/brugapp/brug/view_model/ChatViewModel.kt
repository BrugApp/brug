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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.github.brugapp.brug.*
import com.github.brugapp.brug.data.ACTION_LOST_ERROR_MSG
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.MessageRepository
import com.github.brugapp.brug.data.NETWORK_ERROR_MSG
import com.github.brugapp.brug.model.ChatMessagesListAdapter
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.github.brugapp.brug.ui.ChatActivity
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        viewModelScope.launch(Dispatchers.IO) {
            if (firebaseAuth.currentUser == null) {
                Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    "ERROR: You are no longer logged in ! Log in again to send the message.",
                    Snackbar.LENGTH_LONG
                ).show()
            } else if (!BrugDataCache.isNetworkAvailable()) {
                Toast.makeText(activity, NETWORK_ERROR_MSG, Toast.LENGTH_LONG).show()
            } else {
                val error = if(firebaseAuth.currentUser!!.displayName != null){
                    MessageRepository.addMessageToConv(
                        message,
                        false,
                        firebaseAuth.uid!!,
                        convID,
                        firestore,
                        firebaseAuth,
                        firebaseStorage
                    ).onError
                } else Exception("ERROR: The user name isn't available, aborting.")

                if(error != null){
                    Log.e("FIREBASE ERROR", error.message.toString())
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
        val timestamp = DateService.fromLocalDateTime(LocalDateTime.now())
        val newMessage = LocationMessage(
            "Me",
            DateService.fromLocalDateTime(LocalDateTime.now()),
            strMsg,
            LocationService.fromAndroidLocation(location)
        )

        // Get image from API or create stub one
        val url = getUrlForLocation(activity, location)
        liveData(Dispatchers.IO) {
            emit(loadImageFromUrl(timestamp, url))
        }.observe(activity) { mapImgUri ->
            newMessage.setImageUri(mapImgUri)
        }
        sendMessage(newMessage, convID, activity, firestore, firebaseAuth, firebaseStorage)

        // Clear the message field
        textBox.text = ""

        adapter.notifyItemInserted(messages.size - 1)
    }

    // MUST BE RUN IN A BACKGROUND THREAD !
    private fun loadImageFromUrl(date: DateService, url: URL): Uri {
        try {
            url.openStream().use {
                val mapImgUri = File.createTempFile(simpleDateFormat.format(Date(date.getSeconds())), ".jpg")
                Channels.newChannel(it).use { rbc ->
                    FileOutputStream(mapImgUri).use { fos ->
                        fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
                    }
                }
                return Uri.fromFile(mapImgUri)
            }
        } catch (e: Exception) {
            Log.e("MAPBOX ERROR", e.message.toString())
            Log.e("MAPBOX STATIC API ERROR", "Unable to fetch image")
            return createPlaceholderLocationImage()
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

    fun computeWidthHeight(width: Int, height: Int, targetWidth: Int, targetHeight: Int): Pair<Int, Int>{
        val ratioBM = width.toFloat() / height.toFloat()
        val idealWidth = targetWidth.toFloat()
        val idealHeight = targetHeight.toFloat()

        var resizedWidth = idealWidth
        var resizedHeight = idealHeight

        // the width and height will never exceed 500 by keeping ratio
        if (ratioBM > 1){
            // the width is larger than the height and ratio > 1
            resizedHeight = idealHeight / ratioBM
        }
        else{
            // the height is larger than the width and ratio < 1
            resizedWidth = idealWidth * ratioBM
        }

        return Pair(resizedWidth.toInt(), resizedHeight.toInt())
    }

    private fun compressImage(activity: ChatActivity, uri: Uri): URI {
        val imageBM = uriToBitmap(activity, uri)
        val (resizedWidth, resizedHeight) = computeWidthHeight(imageBM.width, imageBM.height, 500, 500)
        val resized = Bitmap.createScaledBitmap(imageBM, resizedWidth, resizedHeight, false)
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
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        if (checkLocationPermissions(activity)
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
    private fun createPlaceholderLocationImage(): Uri {
        val encodedImage = "iVBORw0KGgoAAAANSUhEUgAAAfQAAAH0CAIAAABEtEjdAAAFXmlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4KPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iWE1QIENvcmUgNS41LjAiPgogPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4KICA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIgogICAgeG1sbnM6ZGM9Imh0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvIgogICAgeG1sbnM6ZXhpZj0iaHR0cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iCiAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgIHhtbG5zOnBob3Rvc2hvcD0iaHR0cDovL25zLmFkb2JlLmNvbS9waG90b3Nob3AvMS4wLyIKICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIKICAgIHhtbG5zOnhtcE1NPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvbW0vIgogICAgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIKICAgZXhpZjpQaXhlbFhEaW1lbnNpb249IjUwMCIKICAgZXhpZjpQaXhlbFlEaW1lbnNpb249IjUwMCIKICAgZXhpZjpDb2xvclNwYWNlPSIxIgogICB0aWZmOkltYWdlV2lkdGg9IjUwMCIKICAgdGlmZjpJbWFnZUxlbmd0aD0iNTAwIgogICB0aWZmOlJlc29sdXRpb25Vbml0PSIyIgogICB0aWZmOlhSZXNvbHV0aW9uPSI3Mi8xIgogICB0aWZmOllSZXNvbHV0aW9uPSI3Mi8xIgogICBwaG90b3Nob3A6Q29sb3JNb2RlPSIzIgogICBwaG90b3Nob3A6SUNDUHJv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"
        val decodedImage = Base64.getDecoder().decode(encodedImage)
        val image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)

        // store to outputstream
        val tempFile = File.createTempFile(simpleDateFormat.format(Date()), ".jpg")
        val outputStream = FileOutputStream(tempFile)
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return Uri.fromFile(tempFile)
    }

    // PERMISSIONS RELATED =======================================================
    fun requestPermissions(context: Context, permissions: Array<String>) {
        val permissionRequestCode = 1 // REQUESTS FOR ALL NON-SET PERMISSIONS IN THE permissions ARRAY
        requestPermissions(context as Activity, permissions, permissionRequestCode)
    }

    fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
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

    private fun requestLocationPermissions(activity: Activity) {
        requestPermissions(
            activity, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), LOCATION_REQUEST_CODE
        )
    }
}

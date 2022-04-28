package com.github.brugapp.brug.view_model

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi

import com.devlomi.record_view.RecordButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.model.ChatMessagesListAdapter
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.github.brugapp.brug.ui.ChatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import java.io.ByteArrayOutputStream
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileDescriptor
import java.net.URI
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

//TODO: NEEDS REFACTORING & DOCUMENTATION
class ChatViewModel : ViewModel() {
    private lateinit var adapter: ChatMessagesListAdapter
    private lateinit var mediaRecorder : MediaRecorder
    private lateinit var mediaPlayer : MediaPlayer
    private lateinit var audioPath : String
    private val RECORDING_REQUEST_CODE = 3000
    private val STORAGE_REQUEST_CODE = 2000
    // For the localisation
    private val locationRequestCode = 1
    private val locationListener = LocationListener { sendLocation(it) }

    //TODO: REMOVE INITIAL INITIALIZATION AND REVERT TO LATEINIT VAR
    // For the list of messages
    private var messages: MutableList<Message> = mutableListOf()

    // For the images
    private lateinit var imageUri: Uri
    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH)
    private val TAKE_PICTURE_REQUEST_CODE = 1
    private val SELECT_PICTURE_REQUEST_CODE = 10

    fun initViewModel(messages: MutableList<Message>) {
        this.messages = messages
        this.adapter = ChatMessagesListAdapter(messages)
    }

//    fun initAdapter() {
//        chatArrayList = arrayListOf()
//        adapter = ChatMessagesListAdapter(chatArrayList)
//    }

    fun getAdapter(): ChatMessagesListAdapter {
        return adapter
    }


    fun sendMessage(content: String, convID: String, activity: AppCompatActivity) {
        val newMessage = Message("Me", DateService.fromLocalDateTime(LocalDateTime.now()), content)
        messages.add(newMessage)

        liveData(Dispatchers.IO) {
            //TODO: REPLACE WITH ACTUAL AUTHENTICATED USER ID
            emit(FirebaseHelper.addMessageToConv(newMessage, "7IsGzvjHKd0KeeKK722m", convID))
        }.observe(activity) { response ->
            if(response.onError != null){
                Snackbar.make(activity.findViewById(android.R.id.content),
                    "ERROR: Unable to register the new message in the database",
                    Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        adapter.notifyItemInserted(messages.size - 1)
    }

    // LOCATION RELATED
    fun requestLocation(
        activity: Activity,
        fusedLocationClient: FusedLocationProviderClient,
        locationManager: LocationManager
    ) {
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
                locationRequestCode
            )
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation: Location? ->
            if (lastKnownLocation != null) {
                sendLocation(lastKnownLocation)
            } else {
                // Launch the locationListener (updates every 1000 ms)
                val locationGpsProvider = LocationManager.GPS_PROVIDER
                locationManager.requestLocationUpdates(
                    locationGpsProvider,
                    50,
                    0.1f
                ) { sendLocation(it) }

                // Stop the update as we only want it once (at least for now)
                locationManager.removeUpdates(locationListener)
            }
        }
    }

    private fun sendLocation(location: Location) {
        //TODO: PROPERLY INITIALIZE NEW MESSAGE IN MESSAGERESPONSE WRAPPER
//        val locationString = "longitude: ${location.longitude}; latitude: ${location.latitude}"
        val newMessage = LocationMessage("Me", DateService.fromLocalDateTime(LocalDateTime.now()), LocationService.fromAndroidLocation(location).toString(), LocationService.fromAndroidLocation(location))//ChatMessage(locationString, 0, LocalDateTime.now(), "Location")
        messages.add(newMessage)
        adapter.notifyItemInserted(messages.size - 1)
        // TODO: Removed from now (to prevent the use of Firebase)
        // sendMessage(locationString)
    }

    // IMAGE RELATED
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
            // Launch the intent (launch the camera)
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
    private fun createImageFile(activity: ChatActivity): File {
        val storageDir: File? = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_",
            ".jpg",
            storageDir
        )
    }

    // TODO: Finish this implementation when the firebase helper is implemented
    private fun uriToBitmap(activity: ChatActivity, selectedFileUri: Uri): Bitmap {
        val parcelFileDescriptor = activity.contentResolver.openFileDescriptor(selectedFileUri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun resize(activity: ChatActivity, uri: Uri): URI {
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadImage(activity: Activity) {
        //val progressDialog = ProgressDialog(activity)
        //progressDialog.setMessage("Uploading image, wait ...")
        //progressDialog.setCancelable(false)
        //progressDialog.show()

        //val fileName = "Image_" + computeDateTime()
        //val storageRef = Firebase.storage.getReference("image/$fileName")

        //storageRef.putFile(imageUri).addOnSuccessListener { ... }

        // resize image and display the new created one
        val resizedUri = resize(activity as ChatActivity, imageUri)
        val newMessage = PicMessage("Me", DateService.fromLocalDateTime(LocalDateTime.now()), "An image", resizedUri.toString())
        messages.add(newMessage)
        adapter.notifyItemInserted(messages.size - 1)
    }


    // ===========================================================================================
    // OLD IMPLEMENTATION USING FIREBASE -> TO BE REFACTORED PROPERLY AFTERWARDS
    // TODO: Have to be removed when the data.DataBase class is implemented
    /*
    private lateinit var db: FirebaseFirestore

    fun eventChangeListener(activity: ChatActivity) {
        // TODO: Change the document when ChatListActivity is implemented
        // TODO: Update code to use data.Database when implemented
        db = Firebase.firestore
        db.collection("Chat").document("User1User2").collection("Messages")
            .orderBy("datetime", Query.Direction.ASCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                @SuppressLint("NotifyDataSetChanged") // Used by the adapter
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        Log.e("Firestore error", error.message.toString())
                        return
                    }

                    // Add retrieved messages to the list of displayed messages
                    for (dc: DocumentChange in value?.documentChanges!!)
                        if (dc.type == DocumentChange.Type.ADDED) {
                            chatArrayList.add(dc.document.toObject(ChatMessage::class.java))
                        }

                    // Notify the adapter to update the list
                    adapter.notifyDataSetChanged()
                    // TODO: Would be cleaner to have this update inside of ChatActivity
                    //activity.updateData(adapter.itemCount - 1)
                    // Instead I have to do this
                    val rv = activity.findViewById<View>(R.id.messagesList) as RecyclerView
                    rv.smoothScrollToPosition(adapter.itemCount - 1)
                }
            })
    }
    */

    fun sendAudio(){

        // TODO: modify this implementation to adapt it for Firestore
        val audioMessage = AudioMessage("Me", DateService.fromLocalDateTime(LocalDateTime.now()),
            "Audio", audioPath)

        messages.add(audioMessage)
        adapter.notifyItemInserted(messages.size - 1)
    }

    fun isAudioPermissionOk(context : Context) : Boolean{
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    fun requestRecording(activity: Activity){
        requestPermissions(activity, Array(1){Manifest.permission.RECORD_AUDIO}, RECORDING_REQUEST_CODE)
    }

    fun isExtStorageOk(context : Context) : Boolean{
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun requestExtStorage(activity: Activity){
        requestPermissions(activity, Array(1){Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE)
    }

    fun setupRecording(){

        audioPath = Environment.getExternalStorageDirectory().absolutePath + "/Documents/audio.3gp"

        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaRecorder.setOutputFile(audioPath)
            mediaRecorder.prepare()
            mediaRecorder.start()
        }catch (e:Exception){
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

}
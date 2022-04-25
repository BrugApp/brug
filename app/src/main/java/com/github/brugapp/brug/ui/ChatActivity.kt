package com.github.brugapp.brug.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.iterator
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.record_view.*
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.ChatMessagesListAdapter
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.view_model.ChatViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private var LOCATION_REQUEST = 1
    private val RECORDING_REQUEST_CODE = 3000
    private val STORAGE_REQUEST_CODE = 2000
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var messageList: RecyclerView

    private lateinit var buttonSendTextMessage : ImageButton
    private lateinit var recordButton : RecordButton
    private lateinit var messageLayout : LinearLayout
    private lateinit var audioRecMessage : TextView
    private lateinit var buttonSendAudio : ImageButton
    private lateinit var deleteAudio : ImageButton
    private lateinit var textMessage : EditText

    private lateinit var conversation: Conversation

    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH)

    @SuppressLint("CutPasteId") // Needed as we read values from EditText fields
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val model: ChatViewModel by viewModels()

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        buttonSendTextMessage = findViewById(R.id.buttonSendMessage)

        initMessageList(model)
        initSendMessageButton(model)
        initSendLocationButton(model, locationManager, fusedLocationClient)
        initSendImageCameraButton(model)
        initSendImageButton(model)

        //initSendImageCameraButton() // TODO: Implement this

        messageLayout = findViewById(R.id.messageLayout)
        audioRecMessage = findViewById(R.id.audioRecording)
        buttonSendAudio = findViewById(R.id.buttonSendAudio)

        recordButton = findViewById(R.id.recordButton)
        model.setListenForRecord(recordButton, false)
        initRecordButton(model)

        deleteAudio = findViewById(R.id.deleteAudio)
        initDeleteAudioButton(model)

        textMessage = findViewById(R.id.editMessage)
        initTextInputField()

        initSendAudioButton(model)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun initMessageList(model: ChatViewModel) {
        conversation = intent.getSerializableExtra(CHAT_INTENT_KEY) as Conversation

        val messageList = findViewById<RecyclerView>(R.id.messagesList)
        model.initViewModel(conversation.messages)
        messageList.layoutManager = LinearLayoutManager(this)

        val adapter = model.getAdapter()
        messageList.adapter = adapter

        adapter.setOnItemClickListener(object: ChatMessagesListAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                if(adapter.getItemViewType(position) == ChatMessagesListAdapter.MessageType.TYPE_LOCATION_RIGHT.ordinal ||
                    adapter.getItemViewType(position) == ChatMessagesListAdapter.MessageType.TYPE_LOCATION_LEFT.ordinal)
                    Toast.makeText(this@ChatActivity, "Map pressed", Toast.LENGTH_LONG).show()
            }
        })

        inflateActionBar(
            conversation.userFields.getFullName(), conversation.lostItemName
        )


    }

    private fun inflateActionBar(username: String, itemLost: String) {
        supportActionBar!!.title = username
        supportActionBar!!.subtitle = "Related to the item \"$itemLost\""
    }

    private fun initSendMessageButton(model: ChatViewModel) {
        // SEND MESSAGE BUTTON
        buttonSendTextMessage.setOnClickListener {
            // Get elements from UI
            val content: String = findViewById<TextView>(R.id.editMessage).text.toString()
            // Clear the message field
            this.findViewById<TextView>(R.id.editMessage).text = ""
            model.sendMessage(content, conversation.convId, this)
        }
    }

    private fun initSendImageCameraButton(model: ChatViewModel) {
        // SEND IMAGE CAMERA BUTTON
        val buttonSendMessage = findViewById<ImageButton>(R.id.buttonSendImagePerCamera)
        buttonSendMessage.setOnClickListener {
            viewModel.takeCameraImage(this)
        }
    }

    private fun initSendImageButton(model: ChatViewModel) {
        // SEND IMAGE BUTTON (from gallery)
        val buttonSendMessage = findViewById<ImageButton>(R.id.buttonSendImage)
        buttonSendMessage.setOnClickListener {
            viewModel.selectGalleryImage(this)
        }
    }

    private fun initSendLocationButton(
        model: ChatViewModel,
        locationManager: LocationManager,
        fusedLocationClient: FusedLocationProviderClient
    ) {
        val buttonSendLocalisation = findViewById<ImageButton>(R.id.buttonSendLocalisation)
        buttonSendLocalisation.setOnClickListener {
            model.requestLocation(this, fusedLocationClient, locationManager)
        }
    }

    // LOCALISATION METHODS (inspired by https://github.com/punit9l/)
    // TODO: Refactor when the firebase helper is implemented
    /*
    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestLocation() {
        // Check if permissions are given
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST)}
        else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { lastKnownLocation: Location? ->
                    if (lastKnownLocation != null) foundLocation(lastKnownLocation)
                }

            // Launch the locationListener (updates every 1000 ms)
            val locationGpsProvider = LocationManager.GPS_PROVIDER
            locationManager.requestLocationUpdates(
                locationGpsProvider,
                1000,
                0.1f,
                locationListener
            )
            // Stop the update as we only want it once (at least for now)
            locationManager.removeUpdates(locationListener)
        }
    }

    // Ask ViewModel to compute UI updates
    @RequiresApi(Build.VERSION_CODES.O)
    private fun foundLocation(location: Location) {
        viewModel.sendLocalisation(location.longitude, location.latitude)
    }

    // Implementation of the LocationListener
    @RequiresApi(Build.VERSION_CODES.O)
    private val locationListener: LocationListener =
        LocationListener { location -> foundLocation(location) }


    // HELPER METHODS TO DISPLAY LIST OF MESSAGES
    private fun initMessageList() {
        messageList = findViewById(R.id.messagesList)
        val linearManager = LinearLayoutManager(this)
        linearManager.stackFromEnd = true
        messageList.layoutManager = linearManager
        messageList.setHasFixedSize(true)

        viewModel.initAdapter()
        messageList.adapter = viewModel.getAdapter()
    }
    */

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val TAKE_PICTURE_REQUEST_CODE = 1
        val SELECT_PICTURE_REQUEST_CODE = 10

        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Image taken", Toast.LENGTH_SHORT).show()
            viewModel.uploadImage(this)
        } else if (requestCode == SELECT_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            val imageUri = data?.data
            if (imageUri != null) {
                viewModel.setImageUri(imageUri)
                viewModel.uploadImage(this)
            }
        }
    }

    /* Function to test if device has a microphone,
    private fun hasMicrophone(): Boolean {
        val pmanager = this.packageManager
        return pmanager.hasSystemFeature(
            PackageManager.FEATURE_MICROPHONE)
    }*/

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == RECORDING_REQUEST_CODE){
            recordButton.isEnabled = true
        }
    }*/

    private fun initRecordButton(model : ChatViewModel) {
        recordButton.setOnClickListener {

            model.setListenForRecord(recordButton, true)

            if(model.isAudioPermissionOk(this) && model.isExtStorageOk(this)){

                model.setupRecording()

                messageLayout.visibility = View.GONE
                recordButton.visibility = View.GONE
                buttonSendAudio.visibility = View.VISIBLE
                deleteAudio.visibility = View.VISIBLE
                audioRecMessage.visibility = View.VISIBLE

            }else if(model.isAudioPermissionOk(this)){
                model.requestExtStorage(this)
            }else if(model.isExtStorageOk(this)){
                model.requestRecording(this)
            }else{
                model.requestRecording(this)
                model.requestExtStorage(this)
            }
        }
    }

    private fun initDeleteAudioButton(model : ChatViewModel){
        deleteAudio.setOnClickListener {
            model.deleteAudio()

            buttonSendAudio.visibility = View.GONE
            deleteAudio.visibility = View.GONE
            audioRecMessage.visibility = View.GONE
            messageLayout.visibility = View.VISIBLE
            recordButton.visibility = View.VISIBLE
            model.setListenForRecord(recordButton, false)
        }
    }

    private fun initSendAudioButton(model : ChatViewModel){
        buttonSendAudio.setOnClickListener {
            model.sendAudio()

            buttonSendAudio.visibility = View.GONE
            deleteAudio.visibility = View.GONE
            audioRecMessage.visibility = View.GONE
            messageLayout.visibility = View.VISIBLE
            recordButton.visibility = View.VISIBLE
            model.setListenForRecord(recordButton, false)

            model.sendMessage("Audio sent, will be able to listen soon ...!", conversation.convId,this)
        }
    }

    private fun initTextInputField(){
        textMessage.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s.toString().trim().isEmpty()) {
                    buttonSendTextMessage.visibility = View.GONE
                    recordButton.visibility = View.VISIBLE
                }else{
                    recordButton.visibility = View.GONE
                    buttonSendTextMessage.visibility = View.VISIBLE
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    @SuppressLint("NewApi")
    fun createFakeImage(): Uri? {
        val encodedImage =
            "iVBORw0KGgoAAAANSUhEUgAAAKQAAACZCAYAAAChUZEyAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAG0SURBVHhe7dIxAcAgEMDALx4rqKKqDxZEZLhbYiDP+/17IGLdQoIhSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSFIMSYohSTEkKYYkxZCkGJIUQ5JiSEJmDnORA7zZz2YFAAAAAElFTkSuQmCC"
        val decodedImage = Base64.getDecoder().decode(encodedImage)
        val image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)

        // store to outputstream
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        // create file name
        val storageDir: File? = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_",
            ".jpg",
            storageDir
        )

        // get URI
        val uri = FileProvider.getUriForFile(
            this,
            "com.github.brugapp.brug.fileprovider",
            imageFile
        )

        // store bitmap to file
        imageFile.writeBytes(outputStream.toByteArray())
        outputStream.flush()
        outputStream.close()

        // return uri
        return uri
    }
}
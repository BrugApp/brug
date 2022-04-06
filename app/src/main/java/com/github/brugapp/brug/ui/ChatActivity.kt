package com.github.brugapp.brug.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.view_model.ChatViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private var LOCATION_REQUEST = 1
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var messageList: RecyclerView

    @SuppressLint("CutPasteId") // Needed as we read values from EditText fields
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val model: ChatViewModel by viewModels()

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initMessageList(model)
        initSendMessageButton(model)
        initSendLocationButton(model, locationManager, fusedLocationClient)
        initSendImageCameraButton(model)
        initSendImageButton(model)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun initMessageList(model: ChatViewModel) {
        val conversation: Conversation =
            intent.getSerializableExtra(CHAT_INTENT_KEY) as Conversation

        val messageList = findViewById<RecyclerView>(R.id.messagesList)

        model.initViewModel(conversation.messages)

        messageList.layoutManager = LinearLayoutManager(this)
        messageList.adapter = model.getAdapter()

        // Set the title bar name with informations related to the conversation
        inflateActionBar(
            "${conversation.user.getFirstName()} ${conversation.user.getLastName()}",
            conversation.lostItem.getName()
        )
    }

    private fun inflateActionBar(username: String, itemLost: String) {
        supportActionBar!!.title = username
        supportActionBar!!.subtitle = "Related to your item \"$itemLost\""
    }

    private fun initSendMessageButton(model: ChatViewModel) {
        // SEND MESSAGE BUTTON
        val buttonSendMessage = findViewById<ImageButton>(R.id.buttonSendMessage)
        buttonSendMessage.setOnClickListener {
            // Get elements from UI
            val content: String = findViewById<TextView>(R.id.editMessage).text.toString()
            // Clear the message field
            this.findViewById<TextView>(R.id.editMessage).text = ""
            model.sendMessage(content)
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
}
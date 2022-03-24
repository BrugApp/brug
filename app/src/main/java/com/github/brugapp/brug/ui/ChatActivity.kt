package com.github.brugapp.brug.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.view_model.ChatViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class ChatActivity : AppCompatActivity() {

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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun initMessageList(model: ChatViewModel) {
        val conversation: Conversation = intent.getSerializableExtra(CHAT_INTENT_KEY) as Conversation

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

    private fun inflateActionBar(username: String, itemLost: String){
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

    private fun initSendLocationButton(
        model: ChatViewModel,
        locationManager: LocationManager,
        fusedLocationClient: FusedLocationProviderClient)
    {
        // SEND LOCALISATION BUTTON
//        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val buttonSendLocalisation = findViewById<ImageButton>(R.id.buttonSendLocalisation)
        buttonSendLocalisation.setOnClickListener {
            model.requestLocation(this, fusedLocationClient, locationManager)
        }
    }

//    // LOCALISATION METHODS (inspired by https://github.com/punit9l/)
//    // TODO: Refactor the class to match the UI/ViewModel architecture
//    private fun requestLocation() {
//        // Check if permissions are given
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions( arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST)}
//        else {
//            fusedLocationClient.lastLocation
//                .addOnSuccessListener { lastKnownLocation: Location? ->
//                    if (lastKnownLocation != null) foundLocation(lastKnownLocation)
//                }
//
//            // Launch the locationListener (updates every 1000 ms)
//            val locationGpsProvider = LocationManager.GPS_PROVIDER
//            locationManager.requestLocationUpdates(
//                locationGpsProvider,
//                1000,
//                0.1f,
//                locationListener
//            )
//            // Stop the update as we only want it once (at least for now)
//            locationManager.removeUpdates(locationListener)
//        }
//    }

//    // Ask ViewModel to compute UI updates
//    private fun foundLocation(location: Location) {
//        viewModel.sendLocalisation(location.longitude, location.latitude)
//    }
//
//    // Implementation of the LocationListener
//    @RequiresApi(Build.VERSION_CODES.O)
//    private val locationListener: LocationListener =
//        LocationListener { location -> foundLocation(location) }
//
//    // HELPER METHODS TO DISPLAY LIST OF MESSAGES
//    private fun initMessageList() {
//        recyclerView = findViewById(R.id.recyclerView)
//        val linearManager = LinearLayoutManager(this)
//        linearManager.stackFromEnd = true
//        recyclerView.layoutManager = linearManager
//        recyclerView.setHasFixedSize(true)
//
//        viewModel.initAdapter()
//        recyclerView.adapter = viewModel.getAdapter()
//    }
}
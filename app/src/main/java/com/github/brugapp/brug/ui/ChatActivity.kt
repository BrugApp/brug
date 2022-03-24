package com.github.brugapp.brug.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.ChatViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView

    private val LOCATION_REQUEST = 1
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("CutPasteId") // Needed as we read values from EditText fields
    @RequiresApi(Build.VERSION_CODES.O) // Needed for the adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // ACTUAL CHAT PART
        initMessageList()
        viewModel.eventChangeListener(this)

        // SEND MESSAGE BUTTON
        val buttonSendMessage = findViewById<ImageButton>(R.id.buttonSendMessage)
        buttonSendMessage.setOnClickListener {
            // Get elements from UI
            val sender: String = this.findViewById<TextView?>(R.id.editName).text.toString()
            val content: String = this.findViewById<TextView?>(R.id.editMessage).text.toString()
            // Clear the message field
            this.findViewById<TextView?>(R.id.editMessage).text = ""
            viewModel.sendMessage(sender, content)
        }

        // SEND LOCALISATION BUTTON
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val buttonSendLocalisation = findViewById<ImageButton>(R.id.buttonSendLocalisation)
        buttonSendLocalisation.setOnClickListener {
            requestLocation()
        }
    }

    // LOCALISATION METHODS (inspired by https://github.com/punit9l/)
    // TODO: Refactor the class to match the UI/ViewModel architecture
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
    @RequiresApi(Build.VERSION_CODES.O) // Needed for the datetime
    private fun foundLocation(location: Location) {
        viewModel.sendLocalisation(location.longitude, location.latitude)
    }

    // Implementation of the LocationListener
    @RequiresApi(Build.VERSION_CODES.O)
    private val locationListener: LocationListener =
        LocationListener { location -> foundLocation(location) }

    // HELPER METHODS TO DISPLAY LIST OF MESSAGES
    private fun initMessageList() {
        recyclerView = findViewById(R.id.recyclerView)
        val linearManager = LinearLayoutManager(this)
        linearManager.stackFromEnd = true
        recyclerView.layoutManager = linearManager
        recyclerView.setHasFixedSize(true)

        viewModel.initAdapter()
        recyclerView.adapter = viewModel.getAdapter()
    }
}
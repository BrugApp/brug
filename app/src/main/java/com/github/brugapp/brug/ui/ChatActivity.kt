package com.github.brugapp.brug.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.provider.Settings
import android.location.LocationManager
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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

        if (!isLocationEnabled()){
            Toast.makeText(this, "Location is disabled!", Toast.LENGTH_LONG).show()
        }

        val buttonSendLocalisation = findViewById<ImageButton>(R.id.buttonSendLocalisation)
        buttonSendLocalisation.setOnClickListener {
            requestLocation()
            // Thanks to https://github.com/punit9l/
        }
    }

    // TODO: Refactor the class to match the UI/ViewModel architecture
    private fun isLocationEnabled(): Boolean {
        val locationMode: Int
        try {
            locationMode = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return false
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST)
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { lastKnownLocation: Location? ->
                    if (lastKnownLocation != null) foundLocation(lastKnownLocation)
                }

            val locationGpsProvider = LocationManager.GPS_PROVIDER
            locationManager.requestLocationUpdates(locationGpsProvider, 1000, 0.1f, locationListener)
            // Stop the update as we only want it once
            locationManager.removeUpdates(locationListener)
        }
    }

    // Ask ViewModel to compute UI updates
    @RequiresApi(Build.VERSION_CODES.O)
    private fun foundLocation(location: Location) {
        viewModel.sendLocalisation(location.longitude, location.latitude)
    }

    // Used for updates in position, maybe needed later? (Actually used once here)
    private val locationListener: LocationListener = object : LocationListener {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onLocationChanged(location: Location) {
            foundLocation(location)
        }


        @RequiresApi(Build.VERSION_CODES.O)
        override fun onProviderEnabled(provider: String) {
            requestLocation()
        }

        override fun onProviderDisabled(provider: String) {
            // TODO ask user to open settings
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    requestLocation() // Permission set, request location
                } else {
                    // permission denied
                }
                return
            }
        }
    }

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

    fun updateData(numberOfMessages: Int) {
        val rv = findViewById<View>(R.id.recyclerView) as RecyclerView
        rv.smoothScrollToPosition(numberOfMessages - 1)
    }
}
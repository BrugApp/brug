package com.github.brugapp.brug.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.databinding.ActivityMapsBinding
import com.github.brugapp.brug.view_model.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


const val EXTRA_LATITUDE = "com.github.brugapp.brug.LATITUDE"
const val EXTRA_LONGITUDE = "com.github.brugapp.brug.LONGITUDE"
const val EXTRA_BRUG_ITEM_NAME = "com.github.brugapp.brug.BRUG_ITEM_NAME"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val viewModel: MapsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.navigateButton).setOnClickListener {
            val gmmIntentUri =
                Uri.parse("google.navigation:q=${viewModel.getDestinationLat()},${viewModel.getDestinationLon()}&mode=w")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        if (intent.extras != null) {
            var destinationLatitude: Double? = null
            var destinationLongitude: Double? = null
            var destinationName: String? = null

            (intent.extras!!.get(EXTRA_LATITUDE) as Double?).apply {
                destinationLatitude = this
            }
            (intent.extras!!.get(EXTRA_LONGITUDE) as Double?).apply {
                destinationLongitude = this
            }
            (intent.extras!!.get(EXTRA_BRUG_ITEM_NAME) as String?).apply {
                destinationName = this
            }

            viewModel.updateDestination(destinationLatitude, destinationLongitude, destinationName)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val marker = LatLng(viewModel.getDestinationLat(), viewModel.getDestinationLon())
        mMap.addMarker(MarkerOptions().position(marker).title(viewModel.getDestinationName()))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
    }

}
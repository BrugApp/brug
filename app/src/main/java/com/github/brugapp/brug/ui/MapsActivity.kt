package com.github.brugapp.brug.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.brugapp.brug.R
import com.github.brugapp.brug.databinding.ActivityMapsBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

        var initialLatitude = -34.0
        var initialLongitude = 151.0
        var name = "Sydney"
        if (intent.extras != null) {
             (intent.extras!!.get(EXTRA_LATITUDE) as Double?)?.apply {
                initialLatitude = this
            }
            (intent.extras!!.get(EXTRA_LONGITUDE) as Double?)?.apply {
                initialLongitude = this
            }
            (intent.extras!!.get(EXTRA_BRUG_ITEM_NAME) as String?)?.apply {
                name = this
            }
        }

        // Add a marker in Sydney and move the camera
        val marker = LatLng(initialLatitude, initialLongitude)
        mMap.addMarker(MarkerOptions().position(marker).title(name))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
    }
}
package com.github.brugapp.brug.view_model

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking

class ItemInformationViewModel : ViewModel() {
    fun getLocationName(lastLocation: LocationService?, geocoder: Geocoder): String {
        if (lastLocation == null) {
            Log.d("ERROR","Localisation null")
            return "Not set"
        }
        val locationName: String = try {
            val addresses = geocoder.getFromLocation(
                lastLocation.getLatitude(),
                lastLocation.getLongitude(),
                1
            )
            if (addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0)
            } else {
                Log.d("ERROR", "addresses empty:")
                "(${lastLocation.getLatitude()}, ${lastLocation.getLongitude()})"
            }
        } catch (e: Exception) {
            Log.d("ERROR", "Exception detected")
            return "Not available"
        }
        return locationName
    }
}
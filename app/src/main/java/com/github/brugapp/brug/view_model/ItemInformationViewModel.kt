package com.github.brugapp.brug.view_model

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking

class ItemInformationViewModel : ViewModel() {

    private lateinit var qrId: String
    private lateinit var item: MyItem

    fun getText(item: MyItem, firebaseAuth: FirebaseAuth,geocoder: Geocoder): HashMap<String, String> {
        this.item = item
        qrId = firebaseAuth.uid + ":" + item.getItemID()
        val hash: HashMap<String, String> = HashMap()
        hash["title"] = item.itemName
        hash["image"] = item.getRelatedIcon().toString()
        hash["lastLocation"] = getLocationName(item.getLastLocation(),geocoder)
        hash["description"] = item.itemDesc
        hash["isLost"] = item.isLost().toString()
        return hash
    }

    private fun getLocationName(lastLocation: LocationService?, geocoder: Geocoder): String {
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

    fun getQrId(): String {
        return qrId
    }

    fun getItemName() : String {
        return item.itemName
    }

}
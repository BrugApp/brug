package com.github.brugapp.brug.view_model

import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.NETWORK_ERROR_MSG
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers

class ItemInformationViewModel : ViewModel() {

    private lateinit var qrId: String
    private lateinit var item: Item

    fun getText(item: Item, firebaseAuth: FirebaseAuth, geocoder: Geocoder, context: AppCompatActivity, observableMap: MutableLiveData<HashMap<String, String>>) {
        this.item = item
        qrId = firebaseAuth.uid + ":" + item.getItemID()

        val observableLocationName = MutableLiveData<String>()
        getLocationName(item.getLastLocation(),geocoder, observableLocationName, context)

        observableMap.postValue(hashMapOf(
            "title" to item.itemName,
            "image" to item.getRelatedIcon().toString(),
            "lastLocation" to "Loading...",
            "description" to item.itemDesc,
            "isLost" to item.isLost().toString()
        ))

        observableLocationName.observe(context){ lastLocationStr ->
            val newMap = observableMap.value
            newMap!!.put("lastLocation", lastLocationStr)
            observableMap.postValue(newMap)
        }
    }

    private fun getLocationName(lastLocation: LocationService?, geocoder: Geocoder, observableName: MutableLiveData<String>, context: AppCompatActivity) {
        liveData(Dispatchers.IO) {
            emit(BrugDataCache.isNetworkAvailable())
        }.observe(context){ status ->
            if(!status) {
                Toast.makeText(context, NETWORK_ERROR_MSG, Toast.LENGTH_LONG).show()
                observableName.postValue("Not available")
            } else if (lastLocation == null) {
                Log.d("ERROR","Localisation null")
                observableName.postValue("Not set")
            } else {
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
                    "Not available"
                }
                observableName.postValue(locationName)
            }

        }
    }

    fun getQrId(): String {
        return qrId
    }

}
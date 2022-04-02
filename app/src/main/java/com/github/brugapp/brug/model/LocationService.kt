package com.github.brugapp.brug.model

import android.location.Location
import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

class LocationService private constructor(val latitude: Double, val longitude: Double): Serializable {
    companion object {
        fun fromGeoPoint(geopoint: GeoPoint): LocationService{
            return LocationService(geopoint.latitude, geopoint.longitude)
        }

        fun fromAndroidLocation(location: Location): LocationService{
            return LocationService(location.latitude, location.longitude)
        }
    }

    fun toAndroidLocation(): Location{
        val location = Location("")
        location.latitude = latitude
        location.longitude = longitude
        return location
    }
}
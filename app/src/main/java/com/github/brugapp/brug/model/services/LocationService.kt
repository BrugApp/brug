package com.github.brugapp.brug.model.services

import android.location.Location
import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

/**
 * Abstraction class handling Location services.
 */
class LocationService private constructor(
    private val latitude: Double,
    private val longitude: Double
) : Serializable {
    companion object {
        fun fromGeoPoint(geopoint: GeoPoint): LocationService {
            return LocationService(geopoint.latitude, geopoint.longitude)
        }

        fun fromAndroidLocation(location: Location): LocationService {
            return LocationService(location.latitude, location.longitude)
        }
    }

    fun toAndroidLocation(): Location {
        val location = Location("")
        location.latitude = latitude
        location.longitude = longitude
        return location
    }

    fun toFirebaseGeoPoint(): GeoPoint {
        return GeoPoint(latitude, longitude)
    }

    override fun toString(): String {
        return "Latitude: $latitude, Longitude: $longitude"
    }

    override fun equals(other: Any?): Boolean {
        val otherLoc = other as LocationService
        return this.longitude == otherLoc.longitude
                && this.latitude == otherLoc.latitude
    }
}
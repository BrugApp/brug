package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel

class MapsViewModel : ViewModel() {
    private var destinationLatitude: Double = 37.410967
    private var destinationLongitude: Double = -122.071387
    private var destinationName: String = "Microsoft"

    fun getDestinationLat(): Double = destinationLatitude
    fun getDestinationLon(): Double = destinationLongitude
    fun getDestinationName(): String = destinationName

    fun updateDestination(destinationLatitude: Double?, destinationLongitude: Double?, destinationName: String?) {
        destinationLatitude?.let{this.destinationLatitude = it}
        destinationLongitude?.let{this.destinationLongitude = it}
        destinationName?.let{this.destinationName = it}
    }
}
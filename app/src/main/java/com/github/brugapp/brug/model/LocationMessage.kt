package com.github.brugapp.brug.model

import android.location.Location
import java.time.LocalDateTime

data class LocationMessage(override val sender: String,
                           override val timestamp: LocalDateTime,
                           override val body: String,
                           private val location: Location)
    : Message(sender, timestamp, body)
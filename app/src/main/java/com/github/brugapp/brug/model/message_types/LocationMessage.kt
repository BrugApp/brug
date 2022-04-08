package com.github.brugapp.brug.model.message_types

import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService

data class LocationMessage(
    override val senderName: String,
    override val timestamp: DateService,
    override val body: String,
    val location: LocationService
) : Message(senderName, timestamp, body)
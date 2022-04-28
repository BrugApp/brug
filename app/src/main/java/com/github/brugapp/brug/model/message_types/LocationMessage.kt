package com.github.brugapp.brug.model.message_types

import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService

data class LocationMessage(
    override val senderName: String,
    override val timestamp: DateService,
    override val body: String,
    val location: LocationService,
    val mapUrl: String
) : Message(senderName, timestamp, body) {
    companion object {
        fun fromMessage(m: Message, location: LocationService): LocationMessage {
            return LocationMessage(
                m.senderName,
                m.timestamp,
                m.body,
                location,
                "mapUrl" //Needs to update for firebase
            )
        }
    }
}

package com.github.brugapp.brug.model

import java.time.LocalDateTime

data class LocationMessage(
    override val sender: String,
    override val timestamp: LocalDateTime,
    override val body: String,
    val location: LocationService)
    : Message(sender, timestamp, body)
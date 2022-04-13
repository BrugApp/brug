package com.github.brugapp.brug.model.message_types

import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService

data class AudioMessage(
    override val senderName: String,
    override val timestamp: DateService,
    override val body: String,
    val audioUrl: String)
    : Message(senderName, timestamp, body)

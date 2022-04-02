package com.github.brugapp.brug.model

import java.time.LocalDateTime

data class AudioMessage(
    override val sender: String,
    override val timestamp: LocalDateTime,
    override val body: String,
    val audioUrl: String)
    : Message(sender, timestamp, body)

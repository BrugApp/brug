package com.github.brugapp.brug.model

import java.time.LocalDateTime

data class PicMessage(
    override val sender: String,
    override val timestamp: LocalDateTime,
    override val body: String,
    private val imgUrl: String)
    : Message(sender, timestamp, body)

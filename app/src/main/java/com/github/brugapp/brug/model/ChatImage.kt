package com.github.brugapp.brug.model

import android.net.Uri
import java.time.LocalDateTime

data class ChatImage(
    val imageURI: String,
    override val sender: String,
    override val mid: Int,
    override val timestamp: LocalDateTime,
    override val body: String
) : Message(mid, timestamp, body, sender)

/*
data class ChatImage(
    val imageURI: Uri,
    override val sender: String,
    override val mid: Int,
    override val timestamp: LocalDateTime,
    override val body: String
) : Message(mid, timestamp, body, sender)
 */
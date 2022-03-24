package com.github.brugapp.brug.model

import java.io.Serializable
import java.time.LocalDateTime

data class ChatMessage(val content: String, val datetime: LocalDateTime, val sender: String): Serializable
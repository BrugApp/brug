package com.github.brugapp.brug.model

import java.io.Serializable
import java.time.LocalDateTime

open class Message(
    open val sender: String,
    open val timestamp: LocalDateTime,
    open val body: String
) : Serializable

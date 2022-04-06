package com.github.brugapp.brug.model

import com.github.brugapp.brug.model.services.DateService
import java.io.Serializable

open class Message(
    open val senderName: String,
    open val timestamp: DateService,
    open val body: String
) : Serializable

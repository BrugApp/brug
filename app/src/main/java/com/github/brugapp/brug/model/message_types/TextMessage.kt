package com.github.brugapp.brug.model.message_types

import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService

data class TextMessage(
    override val senderName: String,
    override val timestamp: DateService,
    override val body: String,
) : Message(senderName, timestamp, body) {

    companion object {
        fun fromMessage(m: Message): TextMessage {
            return TextMessage(m.senderName, m.timestamp, m.body)
        }
    }
}
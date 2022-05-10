package com.github.brugapp.brug.model.message_types

import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService

data class PicMessage(
    override val senderName: String,
    override val timestamp: DateService,
    override val body: String,
    val imgUrl: String
) : Message(senderName, timestamp, body) {

    companion object {
        fun fromMessage(m: Message, imgUrl: String): PicMessage {
            return PicMessage(
                m.senderName,
                m.timestamp,
                m.body,
                imgUrl
            )
        }
    }
}

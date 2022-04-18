package com.github.brugapp.brug.model.message_types

import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService

data class AudioMessage(
    override val senderName: String,
    override val timestamp: DateService,
    override val body: String,
    val audioUrl: String)
    : Message(senderName, timestamp, body){

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && this.audioUrl == (other as AudioMessage).audioUrl
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + senderName.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + audioUrl.hashCode()
        return result
    }
}

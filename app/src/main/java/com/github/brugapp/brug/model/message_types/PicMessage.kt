package com.github.brugapp.brug.model.message_types

import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService

data class PicMessage(
    override val senderName: String,
    override val timestamp: DateService,
    override val body: String,
    val imgUrl: String)
    : Message(senderName, timestamp, body){

    companion object {
        fun fromTextMessage(m: Message, imgUrl: String): PicMessage {
            return PicMessage(
                m.senderName,
                m.timestamp,
                m.body,
                imgUrl
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && this.imgUrl == (other as PicMessage).imgUrl
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + senderName.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + imgUrl.hashCode()
        return result
    }
}

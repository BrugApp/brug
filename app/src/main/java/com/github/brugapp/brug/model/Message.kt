package com.github.brugapp.brug.model

import com.github.brugapp.brug.model.services.DateService
import java.io.Serializable

open class Message(
    open val senderName: String,
    open val timestamp: DateService,
    open val body: String
) : Serializable {
    override fun equals(other: Any?): Boolean {
        val otherMsg = other as Message
        return this.senderName == otherMsg.senderName
                && this.timestamp == otherMsg.timestamp
                && this.body == otherMsg.body
    }

    override fun hashCode(): Int {
        var result = senderName.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + body.hashCode()
        return result
    }
}

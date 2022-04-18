package com.github.brugapp.brug.model

import java.io.Serializable

data class Conversation(
    val convId: String,
    val userFields: MyUser,
    val lostItemName: String,
    val messages: MutableList<Message>
): Serializable
{
    override fun equals(other: Any?): Boolean {
        val otherConv = other as Conversation
        return this.convId == otherConv.convId
                && this.userFields == otherConv.userFields
                && this.lostItemName == otherConv.lostItemName
    }

    override fun hashCode(): Int {
        var result = convId.hashCode()
        result = 31 * result + userFields.hashCode()
        result = 31 * result + lostItemName.hashCode()
        result = 31 * result + messages.hashCode()
        return result
    }
}

package com.github.brugapp.brug.model

import com.github.brugapp.brug.data.DummyUser
import java.io.Serializable

data class Conversation(
    val convId: String,
    val userFields: DummyUser,
    val lostItemName: String,
    val messages: MutableList<Message>
): Serializable

package com.github.brugapp.brug.model

import java.io.Serializable

data class Conversation(
    val convId: String,
    val userFields: User,
    val lostItemName: String,
    val lastMessage: Message?
) : Serializable

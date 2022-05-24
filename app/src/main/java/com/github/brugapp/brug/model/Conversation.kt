package com.github.brugapp.brug.model

import java.io.Serializable

data class Conversation(
    val convId: String,
    val userFields: User,
    val lostItem: Item,
    val lastMessage: Message?
) : Serializable

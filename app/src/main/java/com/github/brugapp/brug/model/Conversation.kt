package com.github.brugapp.brug.model

import java.io.Serializable

data class Conversation(
    val convId: String,
    val userFields: MyUser,
    val lostItemName: String,
    val messages: MutableList<Message>
) : Serializable

package com.github.brugapp.brug.model

import java.io.Serializable

data class Conversation(
    val convId: String,
    val userFields: MyUser,
    val lostItem: MyItem,
    val lastMessage: Message?
) : Serializable

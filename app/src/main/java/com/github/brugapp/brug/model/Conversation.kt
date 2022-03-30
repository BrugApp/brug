package com.github.brugapp.brug.model

import java.io.Serializable

data class Conversation(val user: User, val lostItem: Item, val messages: MutableList<Message>): Serializable

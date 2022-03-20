package com.github.brugapp.brug.model

//TODO: Refactor the class to have a better model of a conversation
data class Conversation(val user: User, val messages: List<String>)

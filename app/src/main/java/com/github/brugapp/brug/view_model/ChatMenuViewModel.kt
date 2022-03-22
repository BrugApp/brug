package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.ChatMessage
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.User

/**
 * ViewModel of the Chat Menu UI, handling its UI logic.
 */
class ChatMenuViewModel : ViewModel() {
    private val myChatList: MutableList<Conversation> by lazy {
        loadConversations()
    }

    private fun loadConversations(): MutableList<Conversation> {
        // TODO in the future: Refactor to fetch values from actual database
        return mutableListOf(
            Conversation(
                User("Anna", "Rosenberg", "anna@rosenberg.com", "123456"),
                listOf(ChatMessage(
                    "Me: Where are you located ? I'm near the center of Lausanne, so feel free to propose me any location in Lausanne",
                    "2022.03.19",
                    "Me"))),
            Conversation(
                User("Henry", "Crawford", "crawform@services.co.uk", "129271"),
                listOf(ChatMessage(
                    "Hey ! I might have found your wallet yesterday near the EPFL campus",
                    "2022.03.19",
                    "Henry"))),
            Conversation(
                User("Jenna", "Hewitt", "jenna.hewitt@epfl.ch", "310827"),
                listOf(ChatMessage(
                    "Fine, lets meet on Saturday then !",
                    "2022.03.19",
                    "Me"))),
            Conversation(
                User("John", "Newmann", "john@microsoft.com", "1892122"),
                listOf(ChatMessage(
                    "Give me my money back you thief !!!",
                    "2022.03.19",
                    "John")))
        )
    }

    /**
     * Getter for the list of conversations.
     */
    fun getChatList(): MutableList<Conversation> {
        return myChatList
    }
}
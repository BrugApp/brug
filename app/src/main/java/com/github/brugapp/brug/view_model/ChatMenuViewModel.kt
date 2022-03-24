package com.github.brugapp.brug.view_model

import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.ChatMessage
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.User
import java.time.LocalDateTime

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
                Item("AirPods", R.drawable.ic_baseline_delete_24, "My beloved AirPods", 0),
                mutableListOf(ChatMessage(
                    "Where are you located ? I'm near the center of Lausanne, so feel free to propose me any location in Lausanne",
                    LocalDateTime.now(),
                    "Me"))),
            Conversation(
                User("Henry", "Crawford", "crawform@services.co.uk", "129271"),
                Item("Wallet", R.drawable.ic_baseline_account_balance_wallet_24, "With all my belongings", 0),
                mutableListOf(ChatMessage(
                    "Hey ! I might have found your wallet yesterday near the EPFL campus",
                    LocalDateTime.now(),
                    "Henry"))),
            Conversation(
                User("Jenna", "Hewitt", "jenna.hewitt@epfl.ch", "310827"),
                Item("Keys", R.drawable.ic_baseline_vpn_key_24, "Home keys", 0),
                mutableListOf(ChatMessage(
                    "Fine, lets meet on Saturday then !",
                    LocalDateTime.now(),
                    "Me"))),
            Conversation(
                User("John", "Newmann", "john@microsoft.com", "1892122"),
                Item("Smartphone", R.drawable.ic_baseline_smartphone_24, "Galaxy S22", 0),
                mutableListOf(ChatMessage(
                    "Give me my money back you thief !!!",
                    LocalDateTime.now(),
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
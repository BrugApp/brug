package com.github.brugapp.brug.view_model

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.fake.MockDatabase
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
        println("===========================================================")
        println(Uri.parse("content://com.github.brugapp.brug.fileprovider/images/JPEG_20220329_165630_1949197185066641075.jpg"))
        println("===========================================================")
        return mutableListOf(
            Conversation(
                User("Anna", "Rosenberg", "anna@rosenberg.com", "123456", null),
                Item("AirPods", "My beloved AirPods", "0"),
                mutableListOf(
                    ChatMessage(
                        "Me",
                        0,
                        LocalDateTime.now(),
                        "Where are you located ? I'm near the center of Lausanne, so feel free to propose me any location in Lausanne"
                    )
                )
            ),
            Conversation(
                User(
                    "Henry",
                    "Crawford",
                    "crawform@services.co.uk",
                    "129271",
                    MockDatabase.currentUser.getProfilePicture()
                ),
                Item("Wallet", "With all my belongings", "0"),
                mutableListOf(
                    ChatMessage(
                        "Henry",
                        0,
                        LocalDateTime.now(),
                        "Hey ! I might have found your wallet yesterday near the EPFL campus"
                    ),
                    ChatMessage(
                        "Me",
                        0,
                        LocalDateTime.now(),
                        "Give me it back!!"
                    )
                )
            ),
            Conversation(
                User("Jenna", "Hewitt", "jenna.hewitt@epfl.ch", "310827", null),
                Item("Keys", "Home keys", "0"),
                mutableListOf(
                    ChatMessage(
                        "Me",
                        0,
                        LocalDateTime.now(),
                        "Fine, lets meet on Saturday then !"
                    )
                )
            ),
            Conversation(
                User("John", "Newmann", "john@microsoft.com", "1892122", null),
                Item("Smartphone", "Galaxy S22", "0"),
                mutableListOf(
                    ChatMessage(
                        "Me",
                        0,
                        LocalDateTime.now(),
                        "Give me my money back you thief !!!"
                    )
                )
            )
        )
    }

    /**
     * Getter for the list of conversations.
     */
    fun getChatList(): MutableList<Conversation> {
        return myChatList
    }
}
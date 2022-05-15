package com.github.brugapp.brug

import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.model.services.DateService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month

class ConversationTest {
    @Test
    fun initConvCorrectlyInitializesConversation() {
        val convID = "DUMMYID"
        val userFields = User("UID", "DUMMYFNAME", "DUMMYLNAME", null, mutableListOf())
        val lostItemName = "DUMMYITEMNAME"
        val lastMessage = Message("SENDERNAME",
                DateService.fromLocalDateTime(
                    LocalDateTime.of(
                    2022, Month.APRIL, 22, 17, 55
                )),
            "TESTMESSAGE")

        val conversation = Conversation(convID, userFields, lostItemName, lastMessage)
        assertThat(conversation.convId, IsEqual(convID))
        assertThat(conversation.userFields, IsEqual(userFields))
        assertThat(conversation.lostItemName, IsEqual(lostItemName))
        assertThat(conversation.lastMessage, IsEqual(lastMessage))
    }

    @Test
    fun compareIdenticalConversationsReturnsEquality() {
        val convID = "DUMMYID"
        val userFields = User("UID", "DUMMYFNAME", "DUMMYLNAME", null, mutableListOf())
        val lostItemName = "DUMMYITEMNAME"
        val lastMessage = Message("SENDERNAME",
                DateService.fromLocalDateTime(
                    LocalDateTime.of(
                        2022, Month.APRIL, 22, 17, 55
                    )),
                "TESTMESSAGE")

        val conversation1 = Conversation(convID, userFields, lostItemName, lastMessage)
        val conversation2 = Conversation(convID, userFields, lostItemName, lastMessage)
        assertThat(conversation1, IsEqual(conversation2))
    }

    @Test
    fun compareAlmostIdenticalConversationsReturnsFalse() {
        val convID = "DUMMYID"
        val userFields = User("UID", "DUMMYFNAME", "DUMMYLNAME", null, mutableListOf())
        val lostItemName = "DUMMYITEMNAME"
        val lastMessage = Message("SENDERNAME",
                DateService.fromLocalDateTime(
                    LocalDateTime.of(
                        2022, Month.APRIL, 22, 17, 55
                    )),
                "TESTMESSAGE")

        val conversation1 = Conversation(convID, userFields, lostItemName, lastMessage)
        val conversation2 = Conversation("DUMMY2", userFields, lostItemName, lastMessage)
        assertThat(conversation1, IsNot(IsEqual(conversation2)))
    }
}
package com.github.brugapp.brug

import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Item
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
        val lostItem = Item("DUMMYITEMNAME", 0, "DUMMYDESC", false)
        val lastMessage = Message("SENDERNAME",
                DateService.fromLocalDateTime(
                    LocalDateTime.of(
                    2022, Month.APRIL, 22, 17, 55
                )),
            "TESTMESSAGE")

        val conversation = Conversation(convID, userFields, lostItem, lastMessage)
        assertThat(conversation.convId, IsEqual(convID))
        assertThat(conversation.userFields, IsEqual(userFields))
        assertThat(conversation.lostItem == lostItem, IsEqual(true))
        assertThat(conversation.lastMessage, IsEqual(lastMessage))
    }

    @Test
    fun compareIdenticalConversationsReturnsEquality() {
        val convID = "DUMMYID"
        val userFields = User("UID", "DUMMYFNAME", "DUMMYLNAME", null, mutableListOf())
        val lostItem = Item("DUMMYITEMNAME", 0, "DUMMYDESC", false)
        val lastMessage = Message("SENDERNAME",
                DateService.fromLocalDateTime(
                    LocalDateTime.of(
                        2022, Month.APRIL, 22, 17, 55
                    )),
                "TESTMESSAGE")

        val conversation1 = Conversation(convID, userFields, lostItem, lastMessage)
        val conversation2 = Conversation(convID, userFields, lostItem, lastMessage)
        assertThat(conversation1, IsEqual(conversation2))
    }

    @Test
    fun compareAlmostIdenticalConversationsReturnsFalse() {
        val convID = "DUMMYID"
        val userFields = User("UID", "DUMMYFNAME", "DUMMYLNAME", null, mutableListOf())
        val lostItem = Item("DUMMYITEMNAME", 0, "DUMMYDESC", false)
        val lastMessage = Message("SENDERNAME",
                DateService.fromLocalDateTime(
                    LocalDateTime.of(
                        2022, Month.APRIL, 22, 17, 55
                    )),
                "TESTMESSAGE")

        val conversation1 = Conversation(convID, userFields, lostItem, lastMessage)
        val conversation2 = Conversation("DUMMY2", userFields, lostItem, lastMessage)
        assertThat(conversation1, IsNot(IsEqual(conversation2)))
    }
}
package com.github.brugapp.brug

import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.MyUser
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
        val userFields = MyUser("UID", "DUMMYFNAME", "DUMMYLNAME", null)
        val lostItemName = "DUMMYITEMNAME"
        val messagesList = mutableListOf(
            Message("SENDERNAME",
                DateService.fromLocalDateTime(
                    LocalDateTime.of(
                    2022, Month.APRIL, 22, 17, 55
                )),
            "TESTMESSAGE")
        )

        val conversation = Conversation(convID, userFields, lostItemName, messagesList)
        assertThat(conversation.convId, IsEqual(convID))
        assertThat(conversation.userFields, IsEqual(userFields))
        assertThat(conversation.lostItemName, IsEqual(lostItemName))
        assertThat(conversation.messages, IsEqual(messagesList))
    }

    @Test
    fun compareIdenticalConversationsReturnsEquality() {
        val convID = "DUMMYID"
        val userFields = MyUser("UID", "DUMMYFNAME", "DUMMYLNAME", null)
        val lostItemName = "DUMMYITEMNAME"
        val messagesList = mutableListOf(
            Message("SENDERNAME",
                DateService.fromLocalDateTime(
                    LocalDateTime.of(
                        2022, Month.APRIL, 22, 17, 55
                    )),
                "TESTMESSAGE")
        )

        val conversation1 = Conversation(convID, userFields, lostItemName, messagesList)
        val conversation2 = Conversation(convID, userFields, lostItemName, messagesList)
        assertThat(conversation1, IsEqual(conversation2))
    }

    @Test
    fun compareAlmostIdenticalConversationsReturnsFalse() {
        val convID = "DUMMYID"
        val userFields = MyUser("UID", "DUMMYFNAME", "DUMMYLNAME", null)
        val lostItemName = "DUMMYITEMNAME"
        val messagesList = mutableListOf(
            Message("SENDERNAME",
                DateService.fromLocalDateTime(
                    LocalDateTime.of(
                        2022, Month.APRIL, 22, 17, 55
                    )),
                "TESTMESSAGE")
        )

        val conversation1 = Conversation(convID, userFields, lostItemName, messagesList)
        val conversation2 = Conversation("DUMMY2", userFields, lostItemName, messagesList)
        assertThat(conversation1, IsNot(IsEqual(conversation2)))
    }
}
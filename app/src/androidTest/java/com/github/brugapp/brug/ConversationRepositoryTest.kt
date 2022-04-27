package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.ConversationRepository
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsEqual
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ConversationRepositoryTest {
    private val uid = "7IsGzvjHKd0KeeKK722m"
    private val convID = "7IsGzvjHKd0KeeKK722mdFtGLE0x08pstMeP68TH"

    @Test
    fun addDocumentMessageTest() {
        val empty = HashMap<String, String>()
        ConversationRepository.addDocumentMessage("userID1", "userID2", empty)
        val task2 = Firebase.firestore.collection("Users").document("userID1" + "userID2")
            .collection("Messages").add(empty)
        assertThat(task2.isSuccessful, `is`(false))
    }

    @Test
    fun getConvReturnsWithoutErrors() = runBlocking {
        val response = ConversationRepository.getConversationsFromUserID(uid)
        assertThat(response.isNullOrEmpty(), IsEqual(false))
    }

    @Test
    fun addMessageReturnsWithoutErrors() = runBlocking {
        val message = Message("Dummy", DateService.fromLocalDateTime(LocalDateTime.now()), "NoBody")
        val response = ConversationRepository.addMessageToConv(message, uid, convID)
        assertThat(response.onError, IsEqual(null))
    }

    //TODO: UNCOMMENT WHEN THE FEATURE WILL BE TOTALLY COMPLETE (I.E. WITH ADD CONVERSATION IMPLEMENTED)
//    @Test
//    fun deleteConvReturnsWithoutErrors() = runBlocking {
//        val response = FirebaseHelper.deleteConvFromID(convID, uid)
//        assertThat(response.onError, IsEqual(null))
//    }
}
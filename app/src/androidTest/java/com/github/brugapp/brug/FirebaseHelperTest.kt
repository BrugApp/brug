package com.github.brugapp.brug

import androidx.lifecycle.liveData
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class FirebaseHelperTest {
    private val uid = "7IsGzvjHKd0KeeKK722m"
    private val convID = "7IsGzvjHKd0KeeKK722mdFtGLE0x08pstMeP68TH"

//    val helper = FirebaseHelper()
    val auth = Firebase.auth
    val firestore = Firebase.firestore

    @Test
    fun getFirebaseAuthTest() {
        assertThat(FirebaseHelper.getFirebaseAuth(), `is`(auth))
    }

    @Test
    fun getCurrentUserTest() {
        assertThat(FirebaseHelper.getCurrentUser(), `is`(auth.currentUser))
    }

    @Test
    fun getCurrentUserIDTest() {
        assertThat(FirebaseHelper.getCurrentUserID(), `is`(auth.currentUser?.uid))
    }

    @Test
    fun getUserCollectionTest() {
        assertThat(FirebaseHelper.getUserCollection(), `is`(firestore.collection("Users")))
    }

    @Test
    fun getChatCollectionTest() {
        assertThat(FirebaseHelper.getChatCollection(), `is`(firestore.collection("Chat")))
    }

    @Test
    fun getChatFromIDPairTest() {
        assertThat(
            FirebaseHelper.getChatFromIDPair("userID1", "userID2"),
            `is`(FirebaseHelper.getChatCollection().document("userID1" + "userID2"))
        )
    }

    @Test
    fun getMessageCollectionTest() {
        assertThat(
            FirebaseHelper.getMessageCollection("userID1", "userID2"),
            `is`(FirebaseHelper.getChatFromIDPair("userID1", "userID2").collection("Messages"))
        )
    }

    @Test
    fun addRegisterUserTaskTest() {
        val empty = HashMap<String, Any>()
        val task1 = FirebaseHelper.addRegisterUserTask(empty)
        val task2 = FirebaseHelper.getUserCollection().add(empty)
        assertThat(task1.isSuccessful, `is`(task2.isSuccessful))
    }

    @Test
    fun addDocumentMessageTest() {
        val empty = HashMap<String, String>()
        val task1 = FirebaseHelper.addDocumentMessage("userID1", "userID2", empty)
        val task2 = FirebaseHelper.getMessageCollection("userID1", "userID2").add(empty)
        assertThat(task1.isSuccessful, `is`(task2.isSuccessful))
    }

    @Test
    fun createNewRegisterUserTest() {
        val user = FirebaseHelper.getCurrentUser()
        val list = listOf<String>()
        val userToAdd = hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (user?.uid ?: String),
            "email" to "emailtxt",
            "firstName" to "firstnametxt",
            "lastName" to "lastnametxt"
        )
        assertThat(
            FirebaseHelper.createNewRegisterUser("emailtxt", "firstnametxt", "lastnametxt"),
            `is`(userToAdd)
        )
    }
    //createAuthAccount is tested by registerUserActivityTest()

    @Test
    fun getConvReturnsWithoutErrors() = runBlocking {
        val response = FirebaseHelper.getConversationsFromUserID(uid)
        assertThat(response.onError, IsEqual(null))
    }

    @Test
    fun addMessageReturnsWithoutErrors() = runBlocking {
        val message = Message("Dummy", DateService.fromLocalDateTime(LocalDateTime.now()), "NoBody")
        val response = FirebaseHelper.addMessageToConv(message, uid, convID)
        assertThat(response.onError, IsEqual(null))
    }

    //TODO: UNCOMMENT WHEN THE FEATURE WILL BE TOTALLY COMPLETE (I.E. WITH ADD CONVERSATION IMPLEMENTED)
//    @Test
//    fun deleteConvReturnsWithoutErrors() = runBlocking {
//        val response = FirebaseHelper.deleteConvFromID(convID, uid)
//        assertThat(response.onError, IsEqual(null))
//    }
}
package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.FirebaseHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseHelperTest {
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
}
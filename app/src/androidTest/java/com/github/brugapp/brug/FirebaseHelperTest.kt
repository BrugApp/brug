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
    val helper = FirebaseHelper()
    val auth = Firebase.auth
    val firestore = Firebase.firestore

    @Test
    fun getFirebaseAuthTest() {
        assertThat(helper.getFirebaseAuth(), `is`(auth))
    }

    @Test
    fun getCurrentUserTest() {
        assertThat(helper.getCurrentUser(), `is`(auth.currentUser))
    }

    @Test
    fun getCurrentUserIDTest() {
        assertThat(helper.getCurrentUserID(), `is`(auth.currentUser?.uid))
    }

    @Test
    fun getUserCollectionTest() {
        assertThat(helper.getUserCollection(), `is`(firestore.collection("Users")))
    }

    @Test
    fun getChatCollectionTest() {
        assertThat(helper.getChatCollection(), `is`(firestore.collection("Chat")))
    }

    @Test
    fun getChatFromIDPairTest() {
        assertThat(
            helper.getChatFromIDPair("userID1", "userID2"),
            `is`(helper.getChatCollection().document("userID1" + "userID2"))
        )
    }

    @Test
    fun getMessageCollectionTest() {
        assertThat(
            helper.getMessageCollection("userID1", "userID2"),
            `is`(helper.getChatFromIDPair("userID1", "userID2").collection("Messages"))
        )
    }

    @Test
    fun addRegisterUserTaskTest() {
        val empty = HashMap<String, Any>()
        val task1 = helper.addRegisterUserTask(empty)
        val task2 = helper.getUserCollection().add(empty)
        assertThat(task1.isSuccessful, `is`(task2.isSuccessful))
    }

    @Test
    fun addDocumentMessageTest() {
        val empty = HashMap<String, String>()
        val task1 = helper.addDocumentMessage("userID1", "userID2", empty)
        val task2 = helper.getMessageCollection("userID1", "userID2").add(empty)
        assertThat(task1.isSuccessful, `is`(task2.isSuccessful))
    }
}
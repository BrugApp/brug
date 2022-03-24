package com.github.brugapp.brug

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.ui.ChatActivity
import com.google.common.base.Predicates.equalTo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.hamcrest.core.Is
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.EnumSet.allOf

@RunWith(AndroidJUnit4::class)
class FirebaseHelperTest {
    val helper = FirebaseHelper()
    val auth = Firebase.auth
    val firestore = Firebase.firestore

    @Test
    fun getFirebaseAuthTest(){
        assert(helper.getFirebaseAuth() == auth)
    }

    @Test
    fun getFirestoreTest(){
        assert(helper.getFirestore() == firestore)
    }

    @Test
    fun getCurrentUserTest(){
        assert(helper.getCurrentUser() == auth.currentUser)
    }

    @Test
    fun getCurrentUserIDTest(){
        assert(helper.getCurrentUserID() == auth.currentUser?.uid)
    }

    @Test
    fun getUserCollectionTest(){
        assert(helper.getUserCollection() == firestore.collection("Users"))
    }

    @Test
    fun getChatCollectionTest(){
        assert(helper.getChatCollection() == firestore.collection("Chat"))
    }

    @Test
    fun getChatFromIDPairTest(){
        assert(helper.getChatFromIDPair("userID1","userID2") == helper.getChatCollection().document("userID1" + "userID2"))
    }

    @Test
    fun getMessageCollectionTest(){
        assert(helper.getMessageCollection("userID1","userID2") == helper.getChatFromIDPair("userID1","userID2").collection("Messages"))
    }

    @Test
    fun addRegisterUserTaskTest(){
        val empty = HashMap<String, Any>()
        val task1 = helper.addRegisterUserTask(empty)
        val task2 = helper.getUserCollection().add(empty)
        assert(task1.isSuccessful == task2.isSuccessful)
    }

    @Test
    fun addDocumentMessageTest(){
        val empty = HashMap<String, String>()
        val task1 = helper.addDocumentMessage("userID1","userID2",empty)
        val task2 = helper.getMessageCollection("userID1", "userID2").add(empty)
        assert(task1.isSuccessful == task2.isSuccessful)
    }
}
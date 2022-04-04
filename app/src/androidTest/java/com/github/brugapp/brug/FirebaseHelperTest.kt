package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.view_model.RegisterUserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseHelperTest {
    val helper = FirebaseHelper()
    val auth = Firebase.auth
    val firestore = Firebase.firestore
    val reg = RegisterUserViewModel()

    @Test
    fun getBadCurrentUserTest() {
        assertThat(helper.getCurrentUser(), IsNull.nullValue())
    }

    @Test
    fun getGoodCurrentUserTest(){
        val list = listOf<String>()
        val userToAdd = hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (auth.uid ?: String),
            "email" to "emailtxt",
            "firstName" to "firstnametxt",
            "lastName" to "lastnametxt"
        )
        helper.addRegisterUser(userToAdd)
        assertThat(helper.getCurrentUser(), IsNull.notNullValue()) //maybe add uid param
    }

    @Test
    fun getBadItemFromCurrentUserTest() {
        assertThat(helper.getItemFromCurrentUser("badObjectID"), IsNull.nullValue())
    }

    @Test
    fun getGoodItemFromCurrentUserTest() {
        val item = Item("name","description","id")
        //val item1 = firestore.collection("Users").document("uid").collection("Items").document("objectID")
        helper.getItemFromCurrentUser("2kmiWr8jzQ37EDX5GAG5")
        //assertThat(helper.getItemFromCurrentUser("2kmiWr8jzQ37EDX5GAG5"), IsNull.notNullValue()) //maybe add uid param
        assertThat(firestore.collection("Users").document("7IsGzvjHKd0KeeKK722m").collection("Items").document("2kmiWr8jzQ37EDX5GAG5"), IsNull.notNullValue()
    }

    @Test
    fun addRegisterUserTest() {
        val empty = HashMap<String, Any>()
        val task1 = helper.addRegisterUser(empty)
        val task2 = Firebase.firestore.collection("Users").add(empty)
        assertThat(task2.isSuccessful, `is`(false))
    }

    @Test
    fun addDocumentMessageTest() {
        val empty = HashMap<String, String>()
        val task1 = helper.addDocumentMessage("userID1", "userID2", empty)
        val task2 = Firebase.firestore.collection("Users").document("userID1" + "userID2")
            .collection("Messages").add(empty)
        assertThat(task2.isSuccessful, `is`(false))
    }

    @Test
    fun createNewRegisterUserTest() {
        val user = helper.getCurrentUser()
        val list = listOf<String>()
        val userToAdd = hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (auth.uid ?: String),
            "email" to "emailtxt",
            "firstName" to "firstnametxt",
            "lastName" to "lastnametxt"
        )
        assertThat(
            helper.createNewRegisterUser("emailtxt", "firstnametxt", "lastnametxt"),
            `is`(userToAdd)
        )
    }
    //createAuthAccount is tested by registerUserActivityTest()
}
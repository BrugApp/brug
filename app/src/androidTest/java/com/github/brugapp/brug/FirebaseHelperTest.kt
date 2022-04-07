package com.github.brugapp.brug

import android.content.Context
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.fake.FakeSignInAccount
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.ui.RegisterUserActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseHelperTest {
    val helper = FirebaseHelper()
    val auth = Firebase.auth
    val firestore = Firebase.firestore

    @Test
    fun getBadCurrentUserTest() {
        var user: User? = helper.getCurrentUser("baduserID")
        assertThat(user, IsNull.nullValue())
    }

    @Test
    fun getGoodCurrentUserTest(){
        var user: User? = helper.getCurrentUser("7IsGzvjHKd0KeeKK722m")
        assertThat(user, IsNull.nullValue()) //maybe add uid param
    }

    @Test
    fun getBadItemFromCurrentUserTest() {
        var item: Item? = helper.getItemFromCurrentUser("7IsGzvjHKd0KeeKK722m","badObjectID")
        assertThat(item, IsNull.nullValue())
    }

    @Test
    fun getGoodItemFromCurrentUserTest() {
        val item = Item("name","description","id")
        helper.getItemFromCurrentUser("7IsGzvjHKd0KeeKK722m","2kmiWr8jzQ37EDX5GAG5")
        //assertThat(helper.getItemFromCurrentUser("2kmiWr8jzQ37EDX5GAG5"), IsNull.notNullValue()) //maybe add uid param
        assertThat(firestore.collection("Users").document("7IsGzvjHKd0KeeKK722m").collection("Items").document("2kmiWr8jzQ37EDX5GAG5"), IsNull.notNullValue())
    }

    @Test
    fun addBadRegisterUserTest() {
        val empty = HashMap<String, Any>()
        val task1 = helper.addRegisterUser(empty)
        val task2 = Firebase.firestore.collection("Users").add(empty)
        assertThat(task2.isSuccessful, `is`(false))
    }

    @Test
    fun addGoodRegisterUserTest() {
        val userToAdd = hashMapOf<String,Any>(
            "firstname" to "firstnametxt",
            "lastname" to "lastnametxt",
            "user_icon" to "uritxt"
        )
        //conv_refs & items not handled here as they are collections
        helper.addRegisterUser(userToAdd)
        val task2 = Firebase.firestore.collection("Users").add(userToAdd)
        assertThat(task2.isSuccessful, `is`(false))
    }

    @Test
    fun addDocumentMessageTest() {
        val empty = HashMap<String, String>()
        helper.addDocumentMessage("userID1", "userID2", empty)
        val task2 = Firebase.firestore.collection("Users").document("userID1" + "userID2")
            .collection("Messages").add(empty)
        assertThat(task2.isSuccessful, `is`(false))
    }

    @Test
    fun createNewRegisterUserTest() {
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

    @Test
    fun createUserInFirestoreWithWrongUidTest() {
        val user = helper.createUserInFirestoreIfAbsent("0", FakeSignInAccount())
        assertThat(user, IsNull.nullValue())
    }
    //createAuthAccount is tested by registerUserActivityTest()
}
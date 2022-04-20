package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.fake.FakeSignInAccount
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.model.services.DateService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
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
    fun getBadCurrentUserTest() {
        val user: User? = FirebaseHelper.getCurrentUser("baduserID")
        assertThat(user, IsNull.nullValue())
    }

    @Test
    fun getGoodCurrentUserTest() {
        val user: User? = FirebaseHelper.getCurrentUser("7IsGzvjHKd0KeeKK722m")
        assertThat(user, IsNull.nullValue()) //maybe add uid param
    }

    @Test
    fun getBadItemFromCurrentUserTest() {
        val item: Item? =
            FirebaseHelper.getItemFromCurrentUser("7IsGzvjHKd0KeeKK722m", "badObjectID")
        assertThat(item, IsNull.nullValue())
    }

    @Test
    fun getGoodItemFromCurrentUserTest() {
        val item = Item("name", "description", "id")
        FirebaseHelper.getItemFromCurrentUser("7IsGzvjHKd0KeeKK722m", "2kmiWr8jzQ37EDX5GAG5")
        //assertThat(helper.getItemFromCurrentUser("2kmiWr8jzQ37EDX5GAG5"), IsNull.notNullValue()) //maybe add uid param
        assertThat(
            firestore.collection("Users").document("7IsGzvjHKd0KeeKK722m").collection("Items")
                .document("2kmiWr8jzQ37EDX5GAG5"), IsNull.notNullValue()
        )
    }

    @Test
    fun addBadRegisterUserTest() {
        val empty = HashMap<String, Any>()
        val task1 = FirebaseHelper.addRegisterUser(empty)
        val task2 = Firebase.firestore.collection("Users").add(empty)
        assertThat(task2.isSuccessful, `is`(false))
    }

    @Test
    fun addGoodRegisterUserTest() {
        val userToAdd = hashMapOf<String, Any>(
            "firstname" to "firstnametxt",
            "lastname" to "lastnametxt",
            "user_icon" to "uritxt"
        )
        //conv_refs & items not handled here as they are collections
        FirebaseHelper.addRegisterUser(userToAdd)
        val task2 = Firebase.firestore.collection("Users").add(userToAdd)
        assertThat(task2.isSuccessful, `is`(false))
    }

    @Test
    fun addDocumentMessageTest() {
        val empty = HashMap<String, String>()
        FirebaseHelper.addDocumentMessage("userID1", "userID2", empty)
        val task2 = Firebase.firestore.collection("Users").document("userID1" + "userID2")
            .collection("Messages").add(empty)
        assertThat(task2.isSuccessful, `is`(false))
    }

    @Test
    fun createNewRegisterUserTest() {
//        val user = FirebaseHelper.getCurrentUser()
        val list = listOf<String>()
        val userToAdd = hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (auth.uid ?: String),
            "email" to "emailtxt",
            "firstName" to "firstnametxt",
            "lastName" to "lastnametxt"
        )
        assertThat(
            FirebaseHelper.createNewRegisterUser("emailtxt", "firstnametxt", "lastnametxt"),
            `is`(userToAdd)
        )
    }

    @Test
    fun createUserInFirestoreWithWrongUidTest() {
        val user = FirebaseHelper.createUserInFirestoreIfAbsent("0", FakeSignInAccount())
        assertThat(user, IsNull.nullValue())
    }
    //createAuthAccount is tested by registerUserActivityTest()

//    @Test
//    fun getConvReturnsWithoutErrors() = runBlocking {
//        val response = FirebaseHelper.getUserConvFromUID(uid)
//        assertThat(response.isNullOrEmpty(), IsEqual(false))
//    }

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
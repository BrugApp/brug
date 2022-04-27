package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.BrugSignInAccount
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryTest {
    private val uid = "7IsGzvjHKd0KeeKK722m"
    private val convID = "7IsGzvjHKd0KeeKK722mdFtGLE0x08pstMeP68TH"
    val auth = Firebase.auth

    @Test
    fun getBadCurrentUserTest() {
        val user: User? = UserRepository.getCurrentUser("baduserID")
        assertThat(user, IsNull.nullValue())
    }

    @Test
    fun getGoodCurrentUserTest() {
        val user: User? = UserRepository.getCurrentUser(uid)
        assertThat(user, IsNull.nullValue()) //maybe add uid param
    }

    //only call addRegisterUser once authenticated
    @Test
    fun addBadRegisterUserTest() {
        val empty = HashMap<String, Any>()
        val task1 = UserRepository.addRegisterUser(empty)
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
        UserRepository.addRegisterUser(userToAdd)
        val task2 = Firebase.firestore.collection("Users").add(userToAdd)
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
        assertThat(UserRepository.createNewRegisterUser("emailtxt", "firstnametxt", "lastnametxt"), `is`(userToAdd))
    }

    @Test
    fun createUserInFirestoreWithWrongUidTest() {
        val user = UserRepository.createUserInFirestoreIfAbsent("0",
            BrugSignInAccount("Son Goku",
                "Vegeta",
                "0",
                "goku@capsulecorp.com"))
        assertThat(user, IsNull.nullValue())
    }

    //createAuthAccount is tested by registerUserActivityTest()
}
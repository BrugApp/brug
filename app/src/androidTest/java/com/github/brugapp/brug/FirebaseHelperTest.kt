package com.github.brugapp.brug

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.BrugSignInAccount
import com.github.brugapp.brug.data.FirebaseHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsEqual
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseHelperTest {
    //    val helper = FirebaseHelper()
    val auth = Firebase.auth

    @Test
    fun addBadRegisterUserTest() {
        val empty = HashMap<String, Any>()
        FirebaseHelper.addRegisterUser(empty)
        val task = Firebase.firestore.collection("Users").add(empty)
        assertThat(task.isSuccessful, `is`(false))
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
    //createAuthAccount is tested by registerUserActivityTest()

    @Test
    fun createAuthAccountCorrectlyAddsNewEntry() = runBlocking {
        val dummyAccount = BrugSignInAccount("DUMMYFNAME", "DUMMYLNAME", "", "dummy@mail.com")
        assertThat(FirebaseHelper.createAuthAccount(dummyAccount, "DUMMYPASSWD").onSuccess, IsEqual(true))
    }
}
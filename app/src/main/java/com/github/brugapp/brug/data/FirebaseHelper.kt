package com.github.brugapp.brug.data

import android.content.ContentValues.TAG
import android.util.Log
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FirebaseHelper {

    private val mAuth: FirebaseAuth = Firebase.auth

    //@TODO functionA for person1 to declare item1 lost
    //@TODO functionB for person2 to declare person1's item1 as found(unlost), creates chat (p1,p2)

    //adds a new user parameter to the Firestore database user collection
    fun addRegisterUser(userToAdd: HashMap<String, Any>) {
        Firebase.firestore.collection("Users").add(userToAdd)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    /*
    Returns a User HashMap object that can be sent to FireBase
    @param  emailtxt  the email of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return the User HashMap object with given parameters that can be sent to firebase
    */
    fun createNewRegisterUser(
        emailtxt: String,
        firstnametxt: String,
        lastnametxt: String
    ): HashMap<String, Any> {
        val list = listOf<String>()
        val userToAdd = hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (mAuth.uid ?: String),
            "email" to emailtxt,
            "firstName" to firstnametxt,
            "lastName" to lastnametxt
        )
        return userToAdd
    }

    /**
     * Creates a new Authentication User entry in Firebase Authentification.
     * @param  account  the SignInAccount object holding the user's fields
     * @param  passwd the password of the user that we are building
     *
     * @return FirebaseResponse object denoting whether or not the adding procedure has been successful
     */
    suspend fun createAuthAccount(
        account: SignInAccount,
        passwd: String,
    ): FirebaseResponse {
        val response = FirebaseResponse()
        try {
            val newAuthEntry = mAuth.createUserWithEmailAndPassword(account.email!!, passwd).await()
            if (newAuthEntry.user == null) {
                response.onError = Exception("Authentication failed.")
                return response
            }

            return UserRepo.addUserFromAccount(newAuthEntry.user!!.uid, account)
        } catch (e: Exception) {
            response.onError = e
        }
        return response
    }
}

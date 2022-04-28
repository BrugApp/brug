package com.github.brugapp.brug.data

import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FirebaseAuthRepository {

    private val mAuth: FirebaseAuth = Firebase.auth

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

            return UserRepository.addUserFromAccount(newAuthEntry.user!!.uid, account)
        } catch (e: Exception) {
            response.onError = e
        }
        return response
    }
}

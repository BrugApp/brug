package com.github.brugapp.brug.di.sign_in.firebase

import com.github.brugapp.brug.di.sign_in.AuthDatabase
import com.github.brugapp.brug.di.sign_in.DatabaseUser
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Adapter class for FirebaseAuth
 *
 * @constructor
 * constructs an AuthFirebase instance from a FirebaseAuth one
 *
 * @param firebaseAuth
 */
class AuthFirebase(firebaseAuth: FirebaseAuth) : AuthDatabase() {

    private val auth: FirebaseAuth = firebaseAuth
    override val currentUser: DatabaseUser?
        get() = auth.currentUser?.let { DatabaseUserFirebase(it) }

    /**
     * Sign out from firebase
     *
     */
    override fun signOut() {
        auth.signOut()
    }

    /**
     * Sign in to firebase with a credential
     *
     * @param credential
     * @return user id on success, null on failure
     */
    override suspend fun signInWithCredential(credential: AuthCredential?): String? {
        return try {
            val result = auth.signInWithCredential(credential!!).await()
            if(result.user != null){
                result.user!!.uid
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * User id
     */
    override val uid: String?
        get() = auth.uid
}
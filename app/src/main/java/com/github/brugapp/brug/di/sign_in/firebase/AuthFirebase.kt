package com.github.brugapp.brug.di.sign_in.firebase

import com.github.brugapp.brug.di.sign_in.AuthDatabase
import com.github.brugapp.brug.di.sign_in.DatabaseUser
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await


class AuthFirebase(firebaseAuth: FirebaseAuth) : AuthDatabase() {

    private val auth: FirebaseAuth = firebaseAuth
    override val currentUser: DatabaseUser?
        get() = auth.currentUser?.let { DatabaseUserFirebase(it) }

    override fun signOut() {
        auth.signOut()
    }

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
//            .addOnCompleteListener(activity) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    activity.updateUI(currentUser)
//                }
//            }
    }

    override val uid: String?
        get() = auth.uid
}
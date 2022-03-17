package com.github.brugapp.brug.di.sign_in.firebase

import com.github.brugapp.brug.di.sign_in.AuthDatabase
import com.github.brugapp.brug.di.sign_in.DatabaseUser
import com.github.brugapp.brug.ui.SignInActivity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class AuthFirebase : AuthDatabase() {

    private val auth: FirebaseAuth = Firebase.auth
    override val currentUser: DatabaseUser?
        get() = auth.currentUser?.let { DatabaseUserFirebase(it) }

    override fun signOut() {
        auth.signOut()
    }

    override fun signInWithCredential(credential: AuthCredential?, activity: SignInActivity) {
        auth.signInWithCredential(credential!!)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    activity.updateUI(currentUser)
                }
            }
    }
}
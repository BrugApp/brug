package com.github.brugapp.brug.di.sign_in


import com.github.brugapp.brug.ui.SignInActivity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

abstract class AuthDatabase {

    abstract val currentUser: DatabaseUser?
    abstract fun signOut()
    abstract fun signInWithCredential(credential: AuthCredential?, activity: SignInActivity)
}

class AuthFirebase : AuthDatabase() {

    private val auth: FirebaseAuth = Firebase.auth
    override val currentUser: DatabaseUser?
        get() = auth.currentUser?.let { DatabaseUserFirebase(it) }

    override fun signOut() {
        auth.signOut()
    }

    override fun signInWithCredential(credential: AuthCredential?, activity: SignInActivity) {
//        var user : FirebaseUser
        auth.signInWithCredential(credential!!)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    activity.updateUI(currentUser)
                }
            }
    }


}


package com.github.brugapp.brug.di.sign_in


import com.google.firebase.auth.AuthCredential

abstract class AuthDatabase {

    abstract val currentUser: DatabaseUser?
    abstract fun signOut()
    abstract suspend fun signInWithCredential(credential: AuthCredential?): String?
    abstract val uid: String?
}


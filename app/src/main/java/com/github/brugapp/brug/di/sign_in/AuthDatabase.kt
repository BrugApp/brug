package com.github.brugapp.brug.di.sign_in


import com.github.brugapp.brug.ui.SignInActivity
import com.google.firebase.auth.AuthCredential

abstract class AuthDatabase {

    abstract val currentUser: DatabaseUser?
    abstract fun signOut()
    abstract fun signInWithCredential(credential: AuthCredential?, activity: SignInActivity)
}


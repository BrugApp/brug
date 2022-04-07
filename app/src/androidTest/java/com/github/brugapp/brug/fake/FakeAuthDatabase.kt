package com.github.brugapp.brug.fake

import com.github.brugapp.brug.di.sign_in.AuthDatabase
import com.github.brugapp.brug.di.sign_in.DatabaseUser
import com.github.brugapp.brug.ui.SignInActivity
import com.google.firebase.auth.AuthCredential

class FakeAuthDatabase(lastUser: DatabaseUser?) : AuthDatabase() {

    private var user: DatabaseUser? = lastUser
    override val currentUser: DatabaseUser?
        get() = user

    override fun signOut() {
        user = null
    }

    override fun signInWithCredential(credential: AuthCredential?, activity: SignInActivity) {
        user = FakeDatabaseUser()
    }

    override val uid: String?
        get() = "0"
}
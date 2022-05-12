package com.github.brugapp.brug.fake

import com.github.brugapp.brug.di.sign_in.AuthDatabase
import com.github.brugapp.brug.di.sign_in.DatabaseUser
import com.google.firebase.auth.AuthCredential

class FakeAuthDatabase(lastUser: DatabaseUser?) : AuthDatabase() {

    private var user: DatabaseUser? = lastUser
    private val dummyUserToken = "wehiuhwauhnxiuauiIUEAUHihiuhuie"
    override val currentUser: DatabaseUser?
        get() = user

    override fun signOut() {
        user = null
    }

    override suspend fun signInWithCredential(credential: AuthCredential?): String {
        user = FakeDatabaseUser()
        return dummyUserToken
    }

    override val uid: String?
        get() = "0"
}
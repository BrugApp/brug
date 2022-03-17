package com.github.brugapp.brug.di.sign_in

import com.google.firebase.auth.FirebaseUser

abstract class DatabaseUser {
    abstract val displayName: String?
    abstract val email: String?
}

class DatabaseUserFirebase(private val account: FirebaseUser) : DatabaseUser() {
    override val displayName: String?
        get() = account.displayName
    override val email: String?
        get() = account.email
}
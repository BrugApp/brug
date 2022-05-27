package com.github.brugapp.brug.di.sign_in.firebase

import com.github.brugapp.brug.di.sign_in.DatabaseUser
import com.google.firebase.auth.FirebaseUser

/**
 * Adapter class for FirebaseUser
 *
 * @property account
 */
class DatabaseUserFirebase(private val account: FirebaseUser) : DatabaseUser() {
    override val displayName: String?
        get() = account.displayName
    override val email: String?
        get() = account.email
}
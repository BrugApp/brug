package com.github.brugapp.brug.fake

import com.github.brugapp.brug.di.sign_in.DatabaseUser

class FakeDatabaseUser : DatabaseUser() {
    override val displayName: String?
        get() = "Son Goku"
    override val email: String?
        get() = "goku@capsulecorp.com"
}
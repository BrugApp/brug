package com.github.brugapp.brug.fake

import com.github.brugapp.brug.di.sign_in.SignInAccount

class FakeSignInAccount : SignInAccount() {
    override val firstName: String
        get() = "Son Goku"
    override val lastName: String
        get() = "Vegeta"
    override val idToken: String
        get() = "0"
    override val email: String
        get() = "goku@capsulecorp.com"
}
package com.github.brugapp.brug.fake

import com.github.brugapp.brug.sign_in.SignInAccount

class FakeSignInAccount : SignInAccount() {
    override val displayName: String
        get() = "Son Goku"
    override val idToken: String
        get() = "0"
    override val email: String
        get() = "goku@capsulecorp.com"
}
package com.github.brugapp.brug.fake

import com.github.brugapp.brug.di.sign_in.SignInAccount

class FakeSignInAccount(val fname: String, val lname: String, val token: String, val mail: String) : SignInAccount() {
    override val firstName: String
        get() = fname
    override val lastName: String
        get() = lname
    override val idToken: String
        get() = token
    override val email: String
        get() = mail
}
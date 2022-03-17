package com.github.brugapp.brug.di.sign_in

import com.google.android.gms.auth.api.signin.GoogleSignInAccount


abstract class SignInAccount {

    abstract val firstName: String?
    abstract val lastName: String?
    abstract val idToken: String?
    abstract val email: String?
}

open class SignInAccountGoogle(private val account: GoogleSignInAccount?) : SignInAccount() {
    override val firstName: String?
        get() = account?.givenName
    override val lastName: String?
        get() = account?.familyName
    override val idToken: String?
        get() = account?.idToken
    override val email: String?
        get() = account?.email

}
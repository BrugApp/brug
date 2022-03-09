package com.github.brugapp.brug.sign_in

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

abstract class SignInAccount {

    abstract val displayName: String?
    abstract val idToken: String?
    abstract val email: String?
}

open class SignInAccountGoogle(private val account: GoogleSignInAccount?) : SignInAccount() {
    override val displayName: String?
        get() = account?.displayName
    override val idToken: String?
        get() = account?.idToken
    override val email: String?
        get() = account?.email
}
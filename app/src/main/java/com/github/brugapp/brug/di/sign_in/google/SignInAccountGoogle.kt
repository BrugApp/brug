package com.github.brugapp.brug.di.sign_in.google

import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Adapter class for GoogleSignInAccount
 *
 * @property account
 */
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
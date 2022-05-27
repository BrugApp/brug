package com.github.brugapp.brug.di.sign_in.google

import android.content.Intent
import com.github.brugapp.brug.di.sign_in.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInClient

/**
 * Adapter class for GoogleSignInClient
 *
 * @property gsc Google Sign In client
 */
class SignInClientGoogle(private val gsc: GoogleSignInClient) : SignInClient() {

    /**
     * Sign in intent
     */
    override val signInIntent: Intent
        get() = gsc.signInIntent

    /**
     * Sign out from Google
     *
     */
    override fun signOut() {
        gsc.signOut()
    }
}
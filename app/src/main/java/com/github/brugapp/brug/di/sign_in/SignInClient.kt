package com.github.brugapp.brug.di.sign_in

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInClient

abstract class SignInClient {

    abstract val signInIntent: Intent

    abstract fun signOut()
}

class SignInClientGoogle(private val gsc: GoogleSignInClient) : SignInClient() {

    override val signInIntent: Intent
        get() = gsc.signInIntent


    override fun signOut() {
        gsc.signOut()
    }

}
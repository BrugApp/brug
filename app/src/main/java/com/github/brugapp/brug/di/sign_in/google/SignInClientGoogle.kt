package com.github.brugapp.brug.di.sign_in.google

import android.content.Intent
import com.github.brugapp.brug.di.sign_in.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class SignInClientGoogle(private val gsc: GoogleSignInClient) : SignInClient() {

    override val signInIntent: Intent
        get() = gsc.signInIntent


    override fun signOut() {
        gsc.signOut()
    }
}
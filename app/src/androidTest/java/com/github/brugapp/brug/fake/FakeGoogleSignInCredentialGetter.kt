package com.github.brugapp.brug.fake

import com.github.brugapp.brug.di.sign_in.SignInCredentialGetter
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

class FakeGoogleSignInCredentialGetter : SignInCredentialGetter() {
    override fun getCredential(idToken: String?): AuthCredential? {
        return GoogleAuthProvider.getCredential("12345", "12345")
    }
}
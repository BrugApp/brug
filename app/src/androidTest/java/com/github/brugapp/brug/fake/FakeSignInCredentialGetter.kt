package com.github.brugapp.brug.fake

import com.github.brugapp.brug.di.sign_in.SignInCredentialGetter
import com.google.firebase.auth.AuthCredential

class FakeSignInCredentialGetter : SignInCredentialGetter() {
    override fun getCredential(idToken: String?): AuthCredential? {
        return null
    }
}
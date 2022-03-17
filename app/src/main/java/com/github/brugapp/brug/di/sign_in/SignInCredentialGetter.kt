package com.github.brugapp.brug.di.sign_in

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

abstract class SignInCredentialGetter {
    abstract fun getCredential(idToken: String?): AuthCredential?
}

class SignInCredentialGetterGoogle : SignInCredentialGetter() {
    override fun getCredential(idToken: String?): AuthCredential {
        return GoogleAuthProvider.getCredential(idToken, null)
    }
}



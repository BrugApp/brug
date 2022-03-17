package com.github.brugapp.brug.di.sign_in.google

import com.github.brugapp.brug.di.sign_in.SignInCredentialGetter
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider


class SignInCredentialGetterGoogle : SignInCredentialGetter() {
    override fun getCredential(idToken: String?): AuthCredential {
        return GoogleAuthProvider.getCredential(idToken, null)
    }
}
package com.github.brugapp.brug.di.sign_in

import com.google.firebase.auth.AuthCredential

abstract class SignInCredentialGetter {
    abstract fun getCredential(idToken: String?): AuthCredential?
}




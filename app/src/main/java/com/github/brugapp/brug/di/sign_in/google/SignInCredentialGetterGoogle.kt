package com.github.brugapp.brug.di.sign_in.google

import com.github.brugapp.brug.di.sign_in.SignInCredentialGetter
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

/**
 * SignInCredentialGetter that get the credentials directly from Google
 *
 */
class SignInCredentialGetterGoogle : SignInCredentialGetter() {
    /**
     * Get credential from Google token id
     *
     * @param idToken
     * @return AuthCredential
     */
    override fun getCredential(idToken: String?): AuthCredential {
        return GoogleAuthProvider.getCredential(idToken, null)
    }
}
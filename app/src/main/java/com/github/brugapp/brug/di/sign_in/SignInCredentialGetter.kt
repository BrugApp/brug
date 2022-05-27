package com.github.brugapp.brug.di.sign_in

import com.google.firebase.auth.AuthCredential

/**
 * Abstract credential getter used in the app
 *
 */
abstract class SignInCredentialGetter {
    /**
     * Get credential from user id
     *
     * @param idToken user id
     * @return AuthCredential?
     */
    abstract fun getCredential(idToken: String?): AuthCredential?
}




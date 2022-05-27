package com.github.brugapp.brug.di.sign_in

import android.content.Intent

/**
 * Abstract Sign in Client instance used in the app
 *
 */
abstract class SignInClient {

    /**
     * Sign in intent
     */
    abstract val signInIntent: Intent

    /**
     * Signs out from client
     *
     */
    abstract fun signOut()
}
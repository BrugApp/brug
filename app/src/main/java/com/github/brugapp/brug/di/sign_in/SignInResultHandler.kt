package com.github.brugapp.brug.di.sign_in

import android.content.Intent

/**
 * Abstract Sign in result handler used in the app
 *
 */
abstract class SignInResultHandler {
    /**
     * Returns account from result intent
     *
     * @param result Intent?
     * @return SIgnInAccount?
     */
    abstract fun handleSignInResult(result: Intent?): SignInAccount?
}



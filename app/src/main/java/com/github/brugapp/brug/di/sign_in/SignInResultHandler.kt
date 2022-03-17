package com.github.brugapp.brug.di.sign_in

import android.content.Intent

abstract class SignInResultHandler {
    abstract fun handleSignInResult(result: Intent?): SignInAccount?
}



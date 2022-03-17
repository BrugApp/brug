package com.github.brugapp.brug.di.sign_in

import android.content.ContentValues
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

abstract class SignInResultHandler {
    abstract fun handleSignInResult(result: Intent?): SignInAccount?
}

class SignInResultHandlerGoogle : SignInResultHandler() {
    private var account: GoogleSignInAccount? = null

    override fun handleSignInResult(result: Intent?): SignInAccount {
        val task: Task<GoogleSignInAccount> =
            GoogleSignIn.getSignedInAccountFromIntent(result)
        getSignInResultFromTask(task)
        return SignInAccountGoogle(account)
    }

    private fun getSignInResultFromTask(task: Task<GoogleSignInAccount>) {
        try {
            account = task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            // Display detailed failure reason.
            Log.w(ContentValues.TAG, "signInResult:failed code=" + e.statusCode)
        }
    }
}



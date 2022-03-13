package com.github.brugapp.brug.sign_in

import android.content.ContentValues
import android.util.Log
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

abstract class SignInResultHandler {
    abstract fun handleSignInResult(result: ActivityResult): SignInAccount?
}

class SignInResultHandlerGoogle : SignInResultHandler() {
    private var account: GoogleSignInAccount? = null
    override fun handleSignInResult(result: ActivityResult): SignInAccount {
        println("I am in correct handler")
        val task: Task<GoogleSignInAccount> =
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
        getSignInResultFromTask(task)
        return SignInAccountGoogle(account)
    }

    private fun getSignInResultFromTask(task: Task<GoogleSignInAccount>) {
        try {
            account = task.getResult(ApiException::class.java)
            println("there was no exception")
        } catch (e: ApiException) {
            // Display detailed failure reason.
            Log.w(ContentValues.TAG, "signInResult:failed code=" + e.statusCode)
            println("there was an exception")
        }
    }
}




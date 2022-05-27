package com.github.brugapp.brug.di.sign_in.google

import android.content.ContentValues
import android.content.Intent
import android.util.Log
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.di.sign_in.SignInResultHandler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

/**
 * Sign in result handler that handles the result with Google API
 *
 */
class SignInResultHandlerGoogle : SignInResultHandler() {
    private var account: GoogleSignInAccount? = null

    /**
     * Returns an account from the result
     *
     * @param result Intent
     * @return SignInAccount
     */
    override fun handleSignInResult(result: Intent?): SignInAccount {
        val task: Task<GoogleSignInAccount> =
            GoogleSignIn.getSignedInAccountFromIntent(result)
        getSignInResultFromTask(task)
        return SignInAccountGoogle(account)
    }

    /**
     * Returns sign in account
     *
     * @param task Task<GoogleSignInAccount>
     */
    private fun getSignInResultFromTask(task: Task<GoogleSignInAccount>) {
        try {
            account = task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            // Display detailed failure reason.
            Log.w(ContentValues.TAG, "signInResult:failed code=" + e.statusCode)
        }
    }
}
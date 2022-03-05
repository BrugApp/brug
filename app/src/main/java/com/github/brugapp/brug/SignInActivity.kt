package com.github.brugapp.brug

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task


const val EXTRA_GOOGLE_ACCOUNT = "com.github.brugapp.brug.google_account"
const val EXTRA_GOOGLE_SIGN_IN_CLIENT = "com.github.brugapp.brug.google_sign_in_client"
lateinit var gsc : GoogleSignInClient

class SignInActivity : AppCompatActivity() {

    // Google Sign-In client

    private var userAccount: GoogleSignInAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        gsc = GoogleSignIn.getClient(this, gso)

        // Set Listener for google sign in button
        findViewById<SignInButton>(R.id.sign_in_google_button).setOnClickListener{
            signInToGoogle(it)
        };

    }

    override fun onStart() {
        super.onStart()

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)

    }

    fun updateUI(account: GoogleSignInAccount?) {
        // If already signed-in, move to next activity
        if (account != null) {
            val intent = Intent(this, SignedInActivity::class.java).apply {
                putExtra(EXTRA_GOOGLE_ACCOUNT, userAccount)
            }
            startActivity(intent)
        }
    }

    val getSignInResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Handle the returned Uri
        if (it.resultCode == Activity.RESULT_OK){
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleSignInResult(task)
        }
    }

    fun signInToGoogle(view: View) {
        println("CLICKED ON SIGN IN {view.id}")
        val signInIntent: Intent = gsc.signInIntent
        getSignInResult.launch(signInIntent)
    }


    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            println("DISPLAY NAME: ${account.displayName}")
            userAccount = account
            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

}
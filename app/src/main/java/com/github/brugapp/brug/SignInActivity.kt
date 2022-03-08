package com.github.brugapp.brug

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.media.session.MediaSession
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.sign_in.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    // Google Sign-In client
    @Inject
    lateinit var signInClient: SignInClient

    @Inject
    lateinit var signInResultHandler: SignInResultHandler

    @set:Inject
    var lastSignedInAccount: SignInAccount? = null
    private var userAccount: SignInAccount? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Set Listener for google sign in button
        findViewById<SignInButton>(R.id.sign_in_google_button).setOnClickListener {
            signIn(it)
        }

        findViewById<Button>(R.id.sign_in_sign_out_button).setOnClickListener {
            signOut(it)
        }

    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        userAccount = lastSignedInAccount
        updateUI(userAccount)
    }

    private fun updateUI(account: SignInAccount?) {
        // If already signed-in display welcome message and sign out button
        if (account != null) {
            findViewById<TextView>(R.id.sign_in_main_text).apply {
                textSize = 16f
                val id = account.idToken
                text = "${getString(R.string.welcome, account.displayName)} \nEmail: ${account.email}"
            }
            findViewById<SignInButton>(R.id.sign_in_google_button).visibility = View.GONE
            findViewById<Button>(R.id.sign_in_sign_out_button).visibility = View.VISIBLE
            // else display sign-in message and button
        } else {
            findViewById<TextView>(R.id.sign_in_main_text).apply {
                textSize = 24f
                text = getString(R.string.action_sign_in)
            }
            findViewById<SignInButton>(R.id.sign_in_google_button).visibility = View.VISIBLE
            findViewById<Button>(R.id.sign_in_sign_out_button).visibility = View.GONE
        }
    }

    val getSignInResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Handle the returned Uri
            if (it.resultCode == Activity.RESULT_OK) {
//                val sintent: Intent = it.data as Intent
//                println(sintent.extras)
                userAccount = signInResultHandler.handleSignInResult(it)
                updateUI(userAccount)
            }
        }

    private fun signIn(view: View) {
        val signInIntent: Intent = signInClient.signInIntent
        getSignInResult.launch(signInIntent)
    }

    private fun signOut(view: View) {
        signInClient.signOut()
        userAccount = null
        updateUI(userAccount)
    }

//    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
//        try {
//            userAccount = SignInAccountGoogle(task.getResult(ApiException::class.java))
//        } catch (e: ApiException) {
//            // Display detailed failure reason.
//            Log.w(ContentValues.TAG, "signInResult:failed code=" + e.statusCode)
//        }
//    }


}
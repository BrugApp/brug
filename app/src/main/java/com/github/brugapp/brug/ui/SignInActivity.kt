package com.github.brugapp.brug.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.MainActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.di.sign_in.DatabaseUser
import com.github.brugapp.brug.view_model.SignInViewModel
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private val viewModel: SignInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Set Listener for google sign in button
        findViewById<SignInButton>(R.id.sign_in_google_button).setOnClickListener {
            signIn()
        }

        findViewById<Button>(R.id.qr_found_btn).setOnClickListener {
            val myIntent = Intent(this, QrCodeScannerActivity::class.java)
            startActivity(myIntent)
        }

        findViewById<Button>(R.id.demo_button).setOnClickListener {
            val myIntent = Intent(this, MainActivity::class.java)
            startActivity(myIntent)
        }

    }

    override fun onStart() {
        super.onStart()
        if (intent.extras != null) {
            val signOutNeeded: Boolean = intent.extras!!.get(EXTRA_SIGN_OUT) as Boolean
            if (signOutNeeded) viewModel.signOut()
        }
        val currentUser = viewModel.getAuth().currentUser
        updateUI(currentUser)
    }

    fun updateUI(user: DatabaseUser?) {
        // If already signed-in display welcome message and sign out button
        if (user != null) {
            val myIntent = Intent(this, ItemsMenuActivity::class.java)
            startActivity(myIntent)
            findViewById<SignInButton>(R.id.sign_in_google_button).visibility = View.GONE
            findViewById<Button>(R.id.qr_found_btn).visibility = View.GONE
        } else {
            findViewById<SignInButton>(R.id.sign_in_google_button).visibility = View.VISIBLE
            findViewById<Button>(R.id.qr_found_btn).visibility = View.VISIBLE
        }
    }

    val getSignInResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Handle the returned Uri
            if (it.resultCode == Activity.RESULT_OK) {
                val credential: AuthCredential? = viewModel.handleSignInResult(it.data)
                firebaseAuth(credential)
            }
        }

    private fun signIn() {
        val signInIntent: Intent = viewModel.getSignInIntent()
        getSignInResult.launch(signInIntent)
    }

    private fun firebaseAuth(credential: AuthCredential?) {
        viewModel.getAuth().signInWithCredential(credential, this)
        val user = viewModel.getAuth().currentUser
        updateUI(user)
    }
}
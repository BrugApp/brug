package com.github.brugapp.brug.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.view_model.SignInViewModel
import com.google.android.gms.common.SignInButton
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

        findViewById<Button>(R.id.sign_in_sign_out_button).setOnClickListener {
            signOut()
        }

    }

    override fun onStart() {
        super.onStart()
        updateUI(viewModel.getCurrentUser())
    }

    private fun updateUI(user: User?) {
        // If already signed-in display welcome message and sign out button
        if (user != null) {
            findViewById<TextView>(R.id.sign_in_main_text).apply {
                textSize = 16f
                text = getString(R.string.welcome_and_email, user.getFirstName(), user.getEmail())
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
                viewModel.handleSignInResult(it.data)
                updateUI(viewModel.getCurrentUser())
            }
        }

    private fun signIn() {
        val signInIntent: Intent = viewModel.getSignInIntent()
        getSignInResult.launch(signInIntent)
    }

    private fun signOut() {
        viewModel.signOut()
        updateUI(viewModel.getCurrentUser())
    }


}
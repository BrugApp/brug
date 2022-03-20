package com.github.brugapp.brug.ui

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.RegisterUserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RegisterUserActivity : AppCompatActivity(), View.OnClickListener {

    private val viewModel: RegisterUserViewModel by viewModels()
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        val regUser = findViewById<Button>(R.id.registerbutton)
        regUser.setOnClickListener(this)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun onClick(v: View?) { //we clicked the register button
        viewModel.storeUserInput(
            FirebaseAuth.getInstance(),
            Firebase.firestore,
            findViewById(R.id.firstname),
            findViewById(R.id.lastName),
            findViewById(R.id.PasswordReg),
            findViewById(R.id.emailAddressReg)
        )
        if (!viewModel.anyEmpty()) {
            onClickHelper()
        }
    }

    private fun onClickHelper() {
        progressBar.visibility = View.VISIBLE
        //make an authentication account with email password tuple
        viewModel.createAuthAccount()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    viewModel.addRegisterUserTask(viewModel.createNewRegisterUser())
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
                    progressBar.visibility = View.GONE //updateUI(user)
                } else { // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        this@RegisterUserActivity,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    progressBar.visibility = View.GONE //updateUI(null)
                }
            }
    }
}
package com.github.brugapp.brug.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.FirebaseAuthRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.view_model.RegisterUserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@AndroidEntryPoint
class RegisterUserActivity : AppCompatActivity() {

    private val viewModel: RegisterUserViewModel by viewModels()
    private lateinit var progressBar: ProgressBar

    @Inject
    lateinit var firestore: FirebaseFirestore
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        initRegForm()
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initRegForm() {
        val regUserButton = findViewById<Button>(R.id.registerbutton)
        val firstNameField = findViewById<EditText>(R.id.firstname)
        val lastNameField = findViewById<EditText>(R.id.lastName)
        val passwdField = findViewById<EditText>(R.id.PasswordReg)
        val emailField = findViewById<EditText>(R.id.emailAddressReg)

        regUserButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if(!viewModel.anyEmpty(firstNameField, lastNameField, passwdField, emailField)){
                val newAccount = BrugSignInAccount(
                    firstNameField.text.toString(),
                    lastNameField.text.toString(),
                    "",
                    emailField.text.toString()
                )

                val response = runBlocking { FirebaseAuthRepository.createAuthAccount(newAccount, passwdField.text.toString(),firebaseAuth,firestore) }
                if(response.onSuccess){
                    Toast.makeText(
                        this,
                        "Authentication was successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "ERROR: Unable to register a new account. Your email address might have already been used for another one",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
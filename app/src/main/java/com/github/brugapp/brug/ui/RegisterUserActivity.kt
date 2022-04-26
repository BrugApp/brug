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
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.data.BrugSignInAccount
import com.github.brugapp.brug.view_model.RegisterUserViewModel
import kotlinx.coroutines.runBlocking


class RegisterUserActivity : AppCompatActivity() {

    private val viewModel: RegisterUserViewModel by viewModels()
    private lateinit var progressBar: ProgressBar

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

                val response = runBlocking { FirebaseHelper.createAuthAccount(newAccount, passwdField.text.toString()) }
                if(response.onSuccess){
                    Toast.makeText(
                        this,
                        "Account creation failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
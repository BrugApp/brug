package com.github.brugapp.brug.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.data.BrugSignInAccount
import com.github.brugapp.brug.view_model.RegisterUserViewModel


class RegisterUserActivity : AppCompatActivity() {

    private val viewModel: RegisterUserViewModel by viewModels()
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        initRegButton()
//        regUser.setOnClickListener(this)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initRegButton() {
        val regUserButton = findViewById<Button>(R.id.registerbutton)
        val firstNameField = findViewById<EditText>(R.id.firstname)
        val lastNameField = findViewById<EditText>(R.id.lastName)
        val passwdField = findViewById<EditText>(R.id.PasswordReg)
        val emailField = findViewById<EditText>(R.id.emailAddressReg)

        regUserButton.setOnClickListener {
            if(!viewModel.anyEmpty(firstNameField, lastNameField, passwdField, emailField)){
                val newAccount = BrugSignInAccount(
                    firstNameField.text.toString(),
                    lastNameField.text.toString(),
                    "",
                    emailField.text.toString()
                )

                FirebaseHelper.createAuthAccount(this, newAccount, passwdField.text.toString())
            }
        }
    }

//    override fun onClick(v: View?) { //we clicked the register button
//        viewModel.storeUserInput(
//            findViewById(R.id.firstname),
//            findViewById(R.id.lastName),
//            findViewById(R.id.PasswordReg),
//            findViewById(R.id.emailAddressReg)
//        )
//        if (!viewModel.anyEmpty()) {
//            onClickHelper()
//        }
//    }
//
//    private fun onClickHelper() {
//        progressBar.visibility = View.VISIBLE
//        //make an authentication account with email password tuple
//        viewModel.createAuthAccount(this@RegisterUserActivity)
//    }
}
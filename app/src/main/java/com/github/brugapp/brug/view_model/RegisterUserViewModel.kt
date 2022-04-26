package com.github.brugapp.brug.view_model

import android.widget.EditText
import android.widget.ProgressBar
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.data.FirebaseHelper

class RegisterUserViewModel : ViewModel() {
//    private val helper: FirebaseHelper = FirebaseHelper()
//    private lateinit var firstnametxt: String
//    private lateinit var lastnametxt: String
//    private lateinit var emailtxt: String
//    private lateinit var passwordtxt: String

    //checks if input data is valid
    fun anyEmpty(firstName: EditText, lastName: EditText, email: EditText, password: EditText): Boolean {
        when {
            firstName.text.isEmpty() -> {
                firstName.error = "Please enter first name"
                firstName.requestFocus()
            }
            lastName.text.isEmpty() -> {
                lastName.error = "Please enter last name"
                lastName.requestFocus()
            }
            password.text.isEmpty() -> {
                password.error = "Please enter password"
                password.requestFocus()
            }
            password.text.length < 6 -> {
                password.error = "Password needs at least 6 characters"
                password.requestFocus()
            }
            email.text.isEmpty() -> {
                email.error = "Please enter email"
                email.requestFocus()
            }
            email.text.filter { it == '@' }.count() != 1 -> {
                email.error = "Please enter valid email"
                email.requestFocus()
            }
            else -> {
                return false
            }
        }
        return true
    }

}
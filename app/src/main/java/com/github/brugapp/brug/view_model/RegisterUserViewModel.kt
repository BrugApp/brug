package com.github.brugapp.brug.view_model

import android.widget.EditText
import android.widget.ProgressBar
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.data.UserRepository

class RegisterUserViewModel : ViewModel() {

    private lateinit var firstnametxt: String
    private lateinit var lastnametxt: String
    private lateinit var emailtxt: String
    private lateinit var passwordtxt: String

    // return new registerUser to add to FireBase
    fun createNewRegisterUser(): HashMap<String, Any> {
        return UserRepository.createNewRegisterUser(emailtxt, firstnametxt, lastnametxt)
    }

    //stores user input data in the view model
    fun storeUserInput(
        firstName: EditText,
        lastName: EditText,
        password: EditText,
        email: EditText
    ) {
        firstnametxt = firstName.text.toString().trim()
        lastnametxt = lastName.text.toString().trim()
        emailtxt = email.text.toString().trim()
        passwordtxt = password.text.toString().trim()
    }

    //checks if input data is valid
    fun anyEmpty(firstName: EditText, lastName: EditText, email: EditText, password: EditText): Boolean {
        when {
            firstnametxt.isEmpty() -> {
                firstName.error = "Please enter first name"
                firstName.requestFocus()
            }
            lastnametxt.isEmpty() -> {
                lastName.error = "Please enter last name"
                lastName.requestFocus()
            }
            passwordtxt.isEmpty() -> {
                password.error = "Please enter password"
                password.requestFocus()
            }
            passwordtxt.length < 6 -> {
                password.error = "Password needs at least 6 characters"
                password.requestFocus()
            }
            emailtxt.isEmpty() -> {
                email.error = "Please enter email"
                email.requestFocus()
            }
            emailtxt.filter { it == '@' }.count() != 1 -> {
                email.error = "Please enter valid email"
                email.requestFocus()
            }
            else -> {
                return false
            }
        }
        return true
    }

    fun addRegisterUserTask(userToAdd: HashMap<String, Any>) {
        return UserRepository.addRegisterUser(userToAdd)
    }

    fun createAuthAccount(context: android.content.Context, progressBar: ProgressBar) {
        return UserRepository.createAuthAccount(context,progressBar, emailtxt, passwordtxt, firstnametxt, lastnametxt)
    }
}
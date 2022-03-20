package com.github.brugapp.brug.view_model

import android.widget.EditText
import android.widget.ProgressBar
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class RegisterUserViewModel : ViewModel() {
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private var firstnametxt = ""
    private var lastnametxt = ""
    private var emailtxt = ""
    private var passwordtxt = ""

    // return new registerUser to add to FireBase
    fun createNewRegisterUser(): HashMap<String, Any> {
        val user = mAuth.currentUser
        val list = listOf<String>()
        val userToAdd = hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (user?.uid ?: String),
            "email" to emailtxt,
            "firstName" to firstnametxt,
            "lastName" to lastnametxt
        )
        return userToAdd
    }

    //stores user input data in the view model
    fun storeUserInput(
        mAuth: FirebaseAuth,
        db: FirebaseFirestore,
        firstName: EditText,
        lastName: EditText,
        password: EditText,
        email: EditText
    ) {
        this.db = db
        this.mAuth = mAuth
        this.firstName = firstName
        this.lastName = lastName
        this.password = password
        this.email = email
        firstnametxt = firstName.text.toString().trim()
        lastnametxt = lastName.text.toString().trim()
        emailtxt = email.text.toString().trim()
        passwordtxt = password.text.toString().trim()
    }

    //checks if input data is valid
    fun anyEmpty(): Boolean {
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

    fun addRegisterUserTask(userToAdd: HashMap<String, Any>): Task<DocumentReference> {
        return db.collection("Users").add(userToAdd)
    }

    fun createAuthAccount(): com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> {
        return mAuth.createUserWithEmailAndPassword(emailtxt, passwordtxt)
    }
}
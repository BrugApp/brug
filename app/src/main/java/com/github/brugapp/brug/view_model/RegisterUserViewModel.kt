package com.github.brugapp.brug.view_model

import android.content.ContentValues
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class RegisterUserViewModel : ViewModel() {
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
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

    fun addRegisterUserTask(userToAdd: HashMap<String, Any>): Task<DocumentReference> {
        return db.collection("Users").add(userToAdd)
    }

    fun createAuthAccount(context: android.content.Context, progressBar: ProgressBar) {
        mAuth.createUserWithEmailAndPassword(emailtxt, passwordtxt)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    addRegisterUserTask(createNewRegisterUser())
                        .addOnSuccessListener { documentReference ->
                            Log.d(
                                ContentValues.TAG,
                                "DocumentSnapshot added with ID: ${documentReference.id}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                ContentValues.TAG,
                                "Error adding document",
                                e
                            )
                        }
                    progressBar.visibility = View.GONE //updateUI(user)
                } else { // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE //updateUI(null)
                }
            }
    }
}
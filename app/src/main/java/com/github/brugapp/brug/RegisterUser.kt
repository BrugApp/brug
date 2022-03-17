package com.github.brugapp.brug

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RegisterUser : AppCompatActivity(), View.OnClickListener{

    private val db = Firebase.firestore
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        mAuth = FirebaseAuth.getInstance()

        val regUser = findViewById<Button>(R.id.registerbutton)
        regUser.setOnClickListener(this)

        progressBar = findViewById(R.id.progressBar)
        firstName = findViewById(R.id.firstname)
        lastName = findViewById(R.id.lastName)
        email = findViewById(R.id.emailAddressReg)
        password = findViewById(R.id.PasswordReg)
    }

    override fun onClick(v: View?) { //we clicked the register button
        firstnametxt = firstName?.text.toString().trim()
        lastnametxt = lastName?.text.toString().trim()
        emailtxt = email?.text.toString().trim()
        passwordtxt = password?.text.toString().trim()
        if (!anyEmpty()) {
            onClickHelper()
        }
    }

    private fun onClickHelper() {
        progressBar?.visibility = View.VISIBLE
        mAuth?.createUserWithEmailAndPassword(emailtxt, passwordtxt)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = mAuth?.currentUser
                    val list = listOf<String>()
                    val userToAdd = hashMapOf("ItemIDArray" to list, "UserID" to (user?.uid ?: String), "email" to emailtxt, "firstName" to firstnametxt, "lastName" to lastnametxt)

                    db.collection("Users").add(userToAdd)
                        .addOnSuccessListener { documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
                    progressBar?.visibility = View.GONE //updateUI(user)
                } else { // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this@RegisterUser, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    progressBar?.visibility = View.GONE //updateUI(null)
                }
            }
    }

    private fun anyEmpty(): Boolean {
        when {
            firstnametxt.isEmpty() -> { firstName?.error = "Please enter first name"
                firstName?.requestFocus()
            }
            lastnametxt.isEmpty() -> { lastName?.error = "Please enter last name"
                lastName?.requestFocus()
            }
            passwordtxt.isEmpty() -> { password?.error = "Please enter password"
                password?.requestFocus()
            }
            passwordtxt.length < 6 -> { password?.error = "Password needs at least 6 characters"
                password?.requestFocus()
            }
            emailtxt.isEmpty() -> { email?.error = "Please enter email"
                email?.requestFocus()
            }
            emailtxt.filter { it == '@' }.count() != 1 -> { email?.error = "Please enter valid email"
                email?.requestFocus()
            }
            else -> { return false }
        }
        return true
    }
}
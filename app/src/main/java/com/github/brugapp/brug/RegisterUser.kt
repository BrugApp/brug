package com.github.brugapp.brug

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.KeyStore


class RegisterUser : AppCompatActivity(), View.OnClickListener{

    private val db = Firebase.firestore
    private var progressBar: ProgressBar? = null
    private var mAuth: FirebaseAuth? = null
    private var firstName: EditText? = null
    private var lastName: EditText? = null
    private var email: EditText? = null
    private var password: EditText? = null
    private var firstnametxt = ""
    private var lastnametxt = ""
    private var emailtxt = ""
    private var passwordtxt = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        mAuth = FirebaseAuth.getInstance()

        var regUser = findViewById<Button>(R.id.registerbutton)
        regUser.setOnClickListener(this)

        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        firstName = findViewById<EditText>(R.id.firstname)
        lastName = findViewById<EditText>(R.id.lastName)
        email = findViewById<EditText>(R.id.emailAddressReg)
        password = findViewById<EditText>(R.id.PasswordReg)
    }

    override fun onClick(v: View?) { //we clicked the register button
        firstnametxt = firstName?.text.toString().trim()
        lastnametxt = lastName?.text.toString().trim()
        emailtxt = email?.text.toString().trim()
        passwordtxt = password?.text.toString().trim()
        if (anyEmpty()) {
            return
        }else{
            onClickHelper()
        }
    }

    fun onClickHelper() {
        progressBar?.visibility = View.VISIBLE
        mAuth?.createUserWithEmailAndPassword(emailtxt, passwordtxt)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = mAuth?.currentUser
                    val list = listOf<String>()
                    val userToAdd = hashMapOf(
                        "ItemIDArray" to list, "UserID" to (user?.uid ?: String), "email" to emailtxt, "firstName" to firstnametxt, "lastName" to lastnametxt)
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

    fun anyEmpty(): Boolean {
        if (firstnametxt.isEmpty()) { firstName?.error = "Please enter first name"
            firstName?.requestFocus() //return
        }
        else if (lastnametxt.isEmpty()) { lastName?.error = "Please enter last name"
            lastName?.requestFocus() //return
        }
        else if (passwordtxt.isEmpty()) { password?.error = "Please enter password"
            password?.requestFocus() //return
        }
        else if (passwordtxt.length < 6) { password?.error = "Password needs at least 6 characters"
            password?.requestFocus()
        } //return
        else if (emailtxt.isEmpty()) { email?.error = "Please enter email"
            email?.requestFocus()
        } //return
        else if (emailtxt.filter { it == '@' }.count() != 1) { email?.error = "Please enter valid email"
            email?.requestFocus()
        } else { return false }
        return true
    }
}
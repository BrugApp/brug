package com.github.brugapp.brug

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase


class RegisterUser : AppCompatActivity(), View.OnClickListener{

    private var mAuth: FirebaseAuth? = null
    private var firstName: EditText? = null
    private var lastName: EditText? = null
    private var email: EditText? = null
    private var password: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        mAuth = FirebaseAuth.getInstance()

        var regUser = findViewById<Button>(R.id.registerbutton)
        regUser.setOnClickListener(this)

        firstName = findViewById<EditText>(R.id.firstname)
        lastName = findViewById<EditText>(R.id.lastName)
        email = findViewById<EditText>(R.id.emailAddressReg)
        password = findViewById<EditText>(R.id.PasswordReg)

    }

    override fun onClick(v: View?) {
        //we clicked the register button
        var firstnametxt = email?.getText().toString().trim()
        var lastNametxt = email?.getText().toString().trim()
        var emailtxt = email?.getText().toString().trim()
        var passwordtxt = email?.getText().toString().trim()

        if(firstnametxt.isEmpty()){
            firstName?.setError("Please enter first name")
            firstName?.requestFocus()
            return
        }
        if(lastNametxt.isEmpty()){
            lastName?.setError("Please enter last name")
            lastName?.requestFocus()
            return
        }
        if(passwordtxt.isEmpty()){
            password?.setError("Please enter password")
            password?.requestFocus()
            return
        }
        if(passwordtxt.length < 6){
            password?.setError("Password needs at least 6 characters")
            password?.requestFocus()
            return
        }
        if(emailtxt.isEmpty()){
            email?.setError("Please enter email")
            email?.requestFocus()
            return
        }
        /*
        if(Patterns.EMAIL_ADDRESS.matcher(emailtxt).matches()){
            email?.setError("Please enter valid email")
            email?.requestFocus()
            return
        }*/
        if(emailtxt.filter { it == '@' }.count()!=1){
            email?.setError("Please enter valid email")
            email?.requestFocus()
            return
        }
        /*
        mAuth?.createUserWithEmailAndPassword(emailtxt, passwordtxt)?.addOnCompleteListener{
            task: Task<AuthResult> ->
            if (task.isSuccessful){
                val user = User(firstnametxt,lastNametxt,emailtxt,passwordtxt);

            }
        }
        */
        mAuth?.createUserWithEmailAndPassword(emailtxt, passwordtxt)
            ?.addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = mAuth!!.currentUser
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        this@RegisterUser, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    //updateUI(null)
                }

                // ...
            }

    }
}
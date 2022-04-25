<<<<<<< Updated upstream
package com.github.brugapp.brug.data

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object UserRepository {
    private val mAuth: FirebaseAuth = Firebase.auth

    //returns the current session's authenticated user
    fun getCurrentUser(userID: String): User? {
        lateinit var firstname: String
        lateinit var lastname: String
        //lateinit var Conv_Refs: MutableList<String>
        //lateinit var Items: MutableList<Item>
        var user: User? = null

        if (mAuth.currentUser != null) {
            val docRef = Firebase.firestore.collection("Users").document(userID)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    if (document.data?.get("firstname") != null && document.data?.get("lastname") != null) {
                        firstname = document.data?.get("firstname") as String
                        lastname = document.data?.get("lastname") as String
                        val email = mAuth.currentUser!!.email
                        val id = mAuth.uid
                        val uri = mAuth.currentUser!!.photoUrl
                        var inputStream: Uri? = null
                        var profilePicture: Drawable? = null

                        if (email != null && id != null) {
                            user = User(firstname, lastname, email, id, profilePicture)
                        }
                    }
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
            //items & conv_ref attributes will be added here later
        }
        return user
    }

    /*
    Returns void after executing a Task<DocumentReference> that tries to create a new FireBase User
    Toasts text corresponding to the task's success/failure
    @param  emailtxt  the email of the user that we are building
    @param  passwordtxt the password of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return void after execution of the Task<DocumentReference> that tries to create a new FireBase User
    */
    fun createAuthAccount(
        context: Context,
        progressBar: ProgressBar,
        emailtxt: String,
        passwordtxt: String,
        firstnametxt: String,
        lastnametxt: String
    ) {
        mAuth.createUserWithEmailAndPassword(emailtxt, passwordtxt)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    addRegisterUser(createNewRegisterUser(emailtxt, firstnametxt, lastnametxt))
                    progressBar.visibility = View.GONE
                } else { // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
    }

    // SignIn
    fun createUserInFirestoreIfAbsent(userId: String?, signInUser: SignInAccount): User? {
        var user: User? = null
        if (userId != null) {
            user = getCurrentUser(userId)
            if (user == null) {
                createNewRegisterUser(
                    signInUser.email!!,
                    signInUser.firstName!!,
                    signInUser.lastName!!
                )
                user = getCurrentUser(userId)
            }
        }
        return user
    }

    //adding auto-generates ID & doc, https://stackoverflow.com/questions/47474522/firestore-difference-between-set-and-add
    //adds a new user parameter to the Firestore database user collection, only use this once authenticated
    fun addRegisterUser(userToAdd: HashMap<String, Any>) {
        Firebase.firestore.collection("Users").add(userToAdd)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    /*
    Returns a User HashMap object that can be sent to FireBase
    @param  emailtxt  the email of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return the User HashMap object with given parameters that can be sent to firebase
    */
    fun createNewRegisterUser(
        emailtxt: String,
        firstnametxt: String,
        lastnametxt: String
    ): HashMap<String, Any> {
        val list = listOf<String>()
        return hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (mAuth.uid ?: String),
            "email" to emailtxt,
            "firstName" to firstnametxt,
            "lastName" to lastnametxt
        )
    }
=======
package com.github.brugapp.brug.data

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object UserRepository {
    private val mAuth: FirebaseAuth = Firebase.auth

    //returns the current session's authenticated user
    fun getCurrentUser(userID: String): User? {
        lateinit var firstname: String
        lateinit var lastname: String
        //lateinit var Conv_Refs: MutableList<String>
        //lateinit var Items: MutableList<Item>
        var user: User? = null

        if (mAuth.currentUser != null) {
            val docRef = Firebase.firestore.collection("Users").document(userID)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    if (document.data?.get("firstname") != null && document.data?.get("lastname") != null) {
                        firstname = document.data?.get("firstname") as String
                        lastname = document.data?.get("lastname") as String
                        val email = mAuth.currentUser!!.email
                        val id = mAuth.uid
                        val uri = mAuth.currentUser!!.photoUrl
                        var inputStream: Uri? = null
                        var profilePicture: Drawable? = null

                        if (email != null && id != null) {
                            user = User(firstname, lastname, email, id, profilePicture)
                        }
                    }
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
            //items & conv_ref attributes will be added here later
        }
        return user
    }

    /*
    Returns void after executing a Task<DocumentReference> that tries to create a new FireBase User
    Toasts text corresponding to the task's success/failure
    @param  emailtxt  the email of the user that we are building
    @param  passwordtxt the password of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return void after execution of the Task<DocumentReference> that tries to create a new FireBase User
    */
    fun createAuthAccount(
        context: Context,
        progressBar: ProgressBar,
        emailtxt: String,
        passwordtxt: String,
        firstnametxt: String,
        lastnametxt: String
    ) {
        mAuth.createUserWithEmailAndPassword(emailtxt, passwordtxt)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    addRegisterUser(createNewRegisterUser(emailtxt, firstnametxt, lastnametxt))
                    progressBar.visibility = View.GONE
                } else { // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
    }

    // SignIn
    fun createUserInFirestoreIfAbsent(userId: String?, signInUser: SignInAccount): User? {
        var user: User? = null
        if (userId != null) {
            user = getCurrentUser(userId)
            if (user == null) {
                createNewRegisterUser(
                    signInUser.email!!,
                    signInUser.firstName!!,
                    signInUser.lastName!!
                )
                user = getCurrentUser(userId)
            }
        }
        return user
    }

    //adding auto-generates ID & doc, https://stackoverflow.com/questions/47474522/firestore-difference-between-set-and-add
    //adds a new user parameter to the Firestore database user collection, only use this once authenticated
    fun addRegisterUser(userToAdd: HashMap<String, Any>) {
        Firebase.firestore.collection("Users").add(userToAdd)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    /*
    Returns a User HashMap object that can be sent to FireBase
    @param  emailtxt  the email of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return the User HashMap object with given parameters that can be sent to firebase
    */
    fun createNewRegisterUser(
        emailtxt: String,
        firstnametxt: String,
        lastnametxt: String
    ): HashMap<String, Any> {
        val list = listOf<String>()
        return hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (mAuth.uid ?: String),
            "email" to emailtxt,
            "firstName" to firstnametxt,
            "lastName" to lastnametxt
        )
    }
>>>>>>> Stashed changes
}
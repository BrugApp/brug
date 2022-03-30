package com.github.brugapp.brug.data

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirebaseHelper {

    private val db: FirebaseFirestore = Firebase.firestore
    private val mAuth: FirebaseAuth = Firebase.auth

    //returns Firestore authentication
    fun getFirebaseAuth(): FirebaseAuth {
        return mAuth
    }

    //returns the current session's authenticated user
    fun getCurrentUser(): FirebaseUser? {
        return mAuth.currentUser
    }

    //returns the unique user ID for the current user
    fun getCurrentUserID(): String? {
        return getCurrentUser()?.uid
    }

    //returns the collection of users from the Firestore database
    fun getUserCollection(): CollectionReference {
        return db.collection("Users")
    }

    //returns the collection of chats from the Firestore database
    fun getChatCollection(): CollectionReference {
        return db.collection("Chat")
    }

    //returns the specific chat document between userID1 and userID2
    fun getChatFromIDPair(userID1: String, userID2: String): DocumentReference {
        return getChatCollection().document(userID1 + userID2)
    }

    //returns the collection of messages from a specific chat between userID1 and userID2
    fun getMessageCollection(userID1: String, userID2: String): CollectionReference {
        return getChatFromIDPair(userID1, userID2).collection("Messages")
    }

    //adds a new user parameter to the Firestore database user collection
    fun addRegisterUserTask(userToAdd: HashMap<String, Any>): Task<DocumentReference> {
        return getUserCollection().add(userToAdd)
    }

    //adds a new message parameter to the Firestore database message collection
    fun addDocumentMessage(
        userID1: String,
        userID2: String,
        message: HashMap<String, String>
    ): Task<DocumentReference> {
        return getMessageCollection(userID1, userID2).add(message)
    }

    /*
    Returns a User HashMap object that can be sent to FireBase
    @param  emailtxt  the email of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return the User HashMap object with given parameters that can be sent to firebase
    */
    fun createNewRegisterUser(emailtxt: String, firstnametxt: String, lastnametxt: String): HashMap<String, Any> {
        val user = getCurrentUser()
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

    /*
    Returns a Task<DocumentReference> that tries to create a new FireBase User
    Toasts text corresponding to the task's success/failure
    @param  emailtxt  the email of the user that we are building
    @param  passwordtxt the password of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return the Task<DocumentReference> that tries to create a new FireBase User
    */
    fun createAuthAccount(context: Context, progressBar: ProgressBar, emailtxt: String, passwordtxt: String, firstnametxt: String, lastnametxt: String){
        mAuth.createUserWithEmailAndPassword(emailtxt, passwordtxt)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    addRegisterUserTask(createNewRegisterUser(emailtxt, firstnametxt, lastnametxt))
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
                    progressBar.visibility = View.GONE
                } else { // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                }
            }
    }
}
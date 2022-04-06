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
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FirebaseHelper {

    val db: FirebaseFirestore = Firebase.firestore
    val mAuth: FirebaseAuth = Firebase.auth

    //returns the current session's authenticated user
    fun getCurrentUser(userID: String): User? {
        lateinit var firstname: String
        lateinit var lastname: String
        //lateinit var Conv_Refs: MutableList<String>
        //lateinit var Items: MutableList<Item>

        if (mAuth.currentUser != null) {
            val docRef = db.collection("Users").document(userID)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    if (document.data?.get("firstname") != null && document.data?.get("lastname") != null) {
                        firstname = document.data?.get("firstname") as String
                        lastname = document.data?.get("lastname") as String
                    }
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }?.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
            val email = mAuth.currentUser!!.email
            val id = mAuth.uid
            val uri = mAuth.currentUser!!.photoUrl
            var inputStream : Uri? = null
            var profilePicture: Drawable? = null

            /* THIS IS HOW TO CONVERT A URI TO A DRAWABLE FOR UI CLASSES
            try {
                inputStream = uri?.let { getContentResolver().openInputStream(it) }
                profilePicture = Drawable.createFromStream(inputStream, uri.toString())
            } catch (e: Exception) {
                print("uri to drawable conversion failed")
            }*/

            if (email == null || id == null || uri == null) { //    ||profilePicture == null
                return null
            } else {
                return User(firstname, lastname, email, id, profilePicture)
            }
            //items & conv_ref attributes will be added here later
        }
        return null
    }

    fun getItemFromCurrentUser(userID: String, objectID: String): Item? {
        lateinit var name: String
        lateinit var description: String
        var is_lost = false //boolean cannot take null type as lateinit default
        var success = false
        lateinit var item_ref: DocumentReference
        lateinit var item_type: String //integer cannot take null type as lateinit default
        val docRef = db.collection("Users").document(userID).collection("Items").document(objectID)
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                if(document.data?.get("is_lost")!=null && document.data?.get("item_description")!=null && document.data?.get("item_type")!=null) {
                    is_lost = document.data?.get("is_lost") as Boolean
                    description = document.data?.get("item_description") as String
                    item_ref = document.data?.get("item_type") as DocumentReference
                    item_type = item_ref.toString().drop(item_ref.toString().length-1)
                    success = true
                }else{
                    is_lost = false
                    description = ""
                    item_type = ""
                }
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
        if (success) {
            val item = Item(name, description, objectID)
            //item.setType(item_type)
            //@TODO convert item_type string to item_type local type
            item.setLost(is_lost)
            return item
        } else {
            return null
        }
    }
    //@TODO functionA for person1 to declare item1 lost
    //@TODO functionB for person2 to declare person1's item1 as found(unlost), creates chat (p1,p2),

    //adds a new user parameter to the Firestore database user collection
    fun addRegisterUser(userToAdd: HashMap<String, Any>) {
        db.collection("Users").add(userToAdd)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    //adds a new message parameter to the Firestore database message collection
    fun addDocumentMessage(userID1: String, userID2: String, message: HashMap<String, String>) {
        db.collection("Chat").document(userID1 + userID2).collection("Messages").add(message)
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
        val userToAdd = hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (mAuth.uid ?: String),
            "email" to emailtxt,
            "firstName" to firstnametxt,
            "lastName" to lastnametxt
        )
        return userToAdd
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
package com.github.brugapp.brug.data

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
}
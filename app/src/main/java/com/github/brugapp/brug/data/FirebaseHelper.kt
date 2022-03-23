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
    fun getFirebaseAuth(): FirebaseAuth {
        return mAuth
    }

    fun getFirestore(): FirebaseFirestore {
        return db
    }

    fun getCurrentUser(): FirebaseUser? {
        return mAuth.currentUser
    }

    fun getCurrentUserID(): String? {
        return getCurrentUser()?.uid
    }

    fun getUserCollection(): CollectionReference{
        return db.collection("Users")
    }

    fun getChatCollection(): CollectionReference {
        return db.collection("Chat")
    }

    fun getChatFromIDPair(userID1: String, userID2: String): DocumentReference{
        return getChatCollection().document(userID1 + userID2)
    }

    fun getMessageCollection(userID1: String, userID2: String): CollectionReference{
        return getChatFromIDPair(userID1,userID2).collection("Messages")
    }

    fun addRegisterUserTask(userToAdd: HashMap<String, Any>): Task<DocumentReference> {
        return getUserCollection().add(userToAdd)
    }

    fun addDocumentMessage(userID1: String, userID2: String, message:  HashMap<String, String>): Task<DocumentReference> {
        return getMessageCollection(userID1, userID2).add(message)
    }
}
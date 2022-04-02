package com.github.brugapp.brug.data

import android.content.ContentValues.TAG
import android.util.Log
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserItemRepository {

//    private val db: FirebaseFirestore = Firebase.firestore
//    private var currentUser : User? = null
//
//    fun getCurrentUser() : User? {
//        return currentUser
//    }
//
//    private fun setCurrentUser(user: User?) {
//        currentUser = user
//    }
//
//    fun getUserFromToken(tokenID: String) : User? {
//        if (currentUser != null && currentUser!!.getId() == tokenID){
//            return currentUser!!
//        }
//        val doc = db.collection("Users").document(tokenID)
//        var user: User? = null
//
//        doc.get()
//            .addOnSuccessListener { document ->
//                if (document != null) {
//                    val firstName = document.data!!["firstname"] as String
//                    val lastName = document.data!!["lastname"] as String
//                    val email = document.data!!["email"] as String
//                    user = User(firstName, lastName, email, tokenID)
//                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
//                } else {
//                    Log.d(TAG, "No such document")
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d(TAG, "get failed with ", exception)
//            }
//        setCurrentUser(user)
//        return user
//    }
//
//    fun createUserInFirestoreIfAbsent(signInUser: SignInAccount): User? {
////        val current = signInUser.idToken?.let { getUserFromToken(it) }
////        if (current != null) return current
//
//        val users = db.collection("Users")
//
//        val data = hashMapOf(
//            "firstname" to signInUser.firstName,
//            "lastname" to signInUser.lastName,
//            "email" to signInUser.email,
//        )
//        users.document(signInUser.idToken!!).set(data)
//
//        setCurrentUser(getUserFromToken(signInUser.idToken!!))
//        return getUserFromToken(signInUser.idToken!!)
//    }
//
//    fun getItemsOfUser(doc: DocumentReference): List<Item> {
//        val items : List<Item> = ArrayList()
//
//        return items
//    }
//
//    fun getItemFromID(userId: String, itemId: String): Item? {
//        val userDoc = db.collection("Users").document(userId)
//        val doc = userDoc.collection("Items").document(itemId)
//        var item: Item? = null
//
//        doc.get()
//            .addOnSuccessListener { document ->
//                if (document != null) {
//                    val lost = document.data!!["is_lost"] as Boolean
//                    val description = document.data!!["item_description"] as String
//                    val name = document.data!!["item_name"] as String
//                    item = Item(name, description, itemId)
//                    item!!.setLost(lost)
//                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
//                } else {
//                    Log.d(TAG, "No such document")
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d(TAG, "get failed with ", exception)
//            }
//        return item
//    }
}
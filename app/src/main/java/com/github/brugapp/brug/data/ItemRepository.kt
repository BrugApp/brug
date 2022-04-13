package com.github.brugapp.brug.data

import android.content.ContentValues.TAG
import android.util.Log
import com.github.brugapp.brug.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object ItemRepository {
    private val mAuth: FirebaseAuth = Firebase.auth

    //@TODO functionA for person1 to declare item1 lost
    //@TODO functionB for person2 to declare person1's item1 as found(unlost), creates chat (p1,p2)

    //returns item from a given user
    fun getItemFromCurrentUser(userID: String, objectID: String): Item? {
        lateinit var name: String
        lateinit var description: String
        var is_lost = false
        var success = false
        lateinit var item_ref: DocumentReference
        lateinit var item_type: String
        val docRef = Firebase.firestore.collection("Users").document(userID).collection("Items")
            .document(objectID)
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                if (document.data?.get("is_lost") != null && document.data?.get("item_description") != null && document.data?.get(
                        "item_type"
                    ) != null
                ) {
                    is_lost = document.data?.get("is_lost") as Boolean
                    description = document.data?.get("item_description") as String
                    item_ref = document.data?.get("item_type") as DocumentReference
                    item_type = item_ref.toString().drop(item_ref.toString().length - 1)
                    success = true
                } else {
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
}
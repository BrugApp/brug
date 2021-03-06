package com.github.brugapp.brug.fake

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class FirebaseFakeHelper {


    fun providesFirestore(): FirebaseFirestore {
        val firestore = Firebase.firestore
        try {
            firestore.useEmulator("10.0.2.2", 8080)
        } catch (e: IllegalStateException) {
            // Already correct emulator
        }
        return firestore
    }

    fun providesStorage(): FirebaseStorage {
        val storage = Firebase.storage
        try {
            storage.useEmulator("10.0.2.2", 9199)
        } catch (e: IllegalStateException) {
            // Already correct emulator
        }
        return storage
    }

    fun providesAuth(): FirebaseAuth {
        val auth = Firebase.auth
        try {
            auth.useEmulator("10.0.2.2",9099)
        }catch (e: IllegalStateException){
            // Already correct emulator
        }
        return auth
    }
}
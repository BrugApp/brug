package com.github.brugapp.brug.data

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import com.github.brugapp.brug.PIC_ATTACHMENT_INTENT_KEY
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File

private const val USERS_ASSETS = "users_assets/"
private const val USERS_DB = "Users"
private const val TOKENS_DB = "Devices"

/**
 * Repository class handling bindings between the User objects in Firebase & in local.
 */
object UserRepository {
    /**
     * Adds a new user after a new account has been created, if the authenticated one is not already present in the database.
     *
     * @param authUID the user ID of the authenticated user
     * @param account the account with informations to populate the new user entry
     *
     * @return FirebaseResponse object, denoting if the new entry has correctly been added to the database
     */
    suspend fun addUserFromAccount(
        authUID: String,
        account: SignInAccount,
        isTest: Boolean,
        firestore: FirebaseFirestore
    ): FirebaseResponse {
        val response = FirebaseResponse()

        try {
            val userDoc = firestore.collection(USERS_DB).document(authUID)
            if (!userDoc.get().await().exists()) {
                userDoc.set(
                    mapOf(
                        "first_name" to account.firstName,
                        "last_name" to account.lastName
                    )
                ).await()
            }

            if(!isTest){
                // ADDS A NEW ENTRY IN THE DEVICE TOKENS LIST
                val deviceToken = FirebaseMessaging.getInstance().token.await()
                Log.e("TOKEN FEEDBACK", deviceToken)
                userDoc.collection(TOKENS_DB).document(deviceToken).set({})
                Log.e("TOKEN FEEDBACK", deviceToken)
            }

            response.onSuccess = true

        } catch (e: Exception) {
            response.onError = e
        }
        return response
    }

    /**
     * Updates the (firstName, lastName) parameters of a given user, just after its fields were updated in local.
     *
     * @param user the user with updated parameters
     *
     * @return FirebaseResponse object, denoting if the entry to update has correctly been updated in Firebase
     */
    suspend fun updateUserFields(user: User, firestore: FirebaseFirestore): FirebaseResponse {
        val response = FirebaseResponse()
        try {
            val userRef = firestore.collection(USERS_DB).document(user.uid)
            if (!userRef.get().await().exists()) {
                response.onError = Exception("User doesn't exist")
                return response
            }
            userRef.update(
                mapOf(
                    "first_name" to user.firstName,
                    "last_name" to user.lastName
                )
            ).await()
            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /**
     * Adds a new device token to a user.
     *
     * @param uid the user id to which the token should be added
     * @param token the token to add
     *
     * @return FirebaseResponse object, denoting if the operation has been successful or not
     */
    fun addNewDeviceTokenToUser(uid: String, token: String, response: MutableLiveData<FirebaseResponse>, firestore: FirebaseFirestore) {
        val userRef = firestore.collection(USERS_DB).document(uid)
        userRef.get().addOnCompleteListener { task ->
            if(task.isSuccessful && task.result.exists()){
                userRef.collection(TOKENS_DB).document(token).set({}).addOnCompleteListener { addToken ->
                    if(addToken.isSuccessful){
                        response.postValue(FirebaseResponse(true, null))
                    } else {
                        response.postValue(FirebaseResponse(false, Exception("TOKEN HAS NOT BEEN PROPERLY ADDED TO THE USER ACCOUNT")))
                    }
                }
            } else {
                response.postValue(FirebaseResponse(false, Exception("THE USER DOESN'T EXIST")))
            }
        }
    }

    /**
     * Deletes a device token from a user.
     *
     * @param uid the user id to which the token should be deleted
     * @param token the token to delete
     *
     * @return FirebaseResponse object, denoting if the operation has been successful or not
     */
    suspend fun deleteDeviceTokenFromUser(
        uid: String,
        token: String,
        firestore: FirebaseFirestore
    ): FirebaseResponse{
        val response = FirebaseResponse()
        try {
            val userRef = firestore.collection(USERS_DB).document(uid)
            if(!userRef.get().await().exists()){
                response.onError = Exception("User doesn't exist")
                return response
            }
            // REMOVES THE TOKEN IF IT EXISTS
            val tokenRef = userRef.collection(TOKENS_DB).document(token)
            if(!tokenRef.get().await().exists()){
                response.onError = Exception("Token doesn't exist")
                return response
            }
            userRef.collection(TOKENS_DB).document(token).delete().await()
            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /**
     * Get Uri from Intent.
     *
     * @param data the intent to get the uri from
     *
     * @return Uri of the image
     */
    fun getImageUri(data: Intent?): Uri {
        return data!!.data ?: Uri.parse(data.extras?.getString(PIC_ATTACHMENT_INTENT_KEY))
    }

    /**
     * Uploads & updates the userIcon parameter of a given user to Firebase, just after it was updated in local.
     *
     * @param uid the user ID for which the image will be modified
     * @param newIcon the image to upload
     *
     * @return FirebaseResponse object, denoting if the upload + update to Firebase was successful
     */
    suspend fun updateUserIcon(
        uid: String,
        newIcon: Drawable,
        auth: FirebaseAuth,
        firebaseStorage: FirebaseStorage,
        firestore: FirebaseFirestore
    ): FirebaseResponse {
        val response = FirebaseResponse()
        try {
            // Uploading to Firebase Storage requires a signed-in user !
            val currentUser = auth.currentUser
            if (currentUser == null) { //|| currentUser.uid != uid NOT FOR THE MOMENT, SINCE USER ISN'T IN AUTH DATABASE
                response.onError = Exception("User is not signed in")
                return response
            }

            val iconPath = "$USERS_ASSETS$uid/$uid.jpg"

            val bitmap = newIcon
                .toBitmap(newIcon.intrinsicWidth, newIcon.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            // FIRST UPLOAD THE FILE TO THE LOCATION
            firebaseStorage.reference.child(iconPath).putBytes(data).await()

            // THEN UPDATE THE FIELD IN THE USER DOCUMENT
            firestore.collection(USERS_DB).document(uid).update("user_icon", iconPath).await()

            baos.close()
            response.onSuccess = true

        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /**
     * Deletes a user from the database, given a user ID.
     *
     * @param uid the user ID
     *
     * @return FirebaseResponse object, denoting if the deletion was successful
     */
    suspend fun deleteUserFromID(uid: String, firestore: FirebaseFirestore): FirebaseResponse {
        val response = FirebaseResponse()
        try {
            val userRef = firestore.collection(USERS_DB).document(uid)
            if (!userRef.get().await().exists()) {
                response.onError = Exception("User doesn't exist")
                return response
            }

            firestore.collection(USERS_DB).document(uid).delete().await()
            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /**
     * Fetches a user without its Items nor its Conversation objects, given a user ID.
     *
     * @param uid the user ID
     *
     * @return MyUser object instantiated with the parameters held in Firebase, or a null value in case of error
     */
    suspend fun getUserFromUID(
        uid: String,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): User? {
        try {
            val userRef = firestore.collection(USERS_DB).document(uid)
            val userDoc = userRef.get().await()
            if (!userDoc.contains("first_name")
                || !userDoc.contains("last_name")
            ) {
                Log.e("FIREBASE ERROR", "Invalid User Format")
                return null
            }

            // RETRIEVE THE LIST OF DEVICE TOKENS
            val tokensList = userRef.collection(TOKENS_DB).get().await().mapNotNull { tokenDoc ->
                tokenDoc.id
            }.toMutableList()

            val userIconPath = if (userDoc.contains("user_icon")) {
                downloadUserIconInTemp(
                    uid, userDoc["user_icon"] as String,
                    firebaseAuth, firebaseStorage
                )
            } else null

            return User(
                    uid,
                    userDoc["first_name"] as String,
                    userDoc["last_name"] as String,
                    userIconPath,
                    tokensList
                )
        } catch (e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            return null
        }
    }

    /* ONLY FOR TEST PURPOSES */
    suspend fun resetUserIcon(uid: String, firestore: FirebaseFirestore): FirebaseResponse {
        val response = FirebaseResponse()
        try {
            val userRef = firestore.collection(USERS_DB).document(uid)
            if (!userRef.get().await().exists()) {
                response.onError = Exception("User doesn't exist")
                return response
            }

            firestore.collection(USERS_DB).document(uid).update(
                mapOf(
                    "user_icon" to ""
                )
            ).await()
            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    private suspend fun downloadUserIconInTemp(
        uid: String, path: String,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): String? {
        try {
            val file = File.createTempFile(uid, ".jpg")
            // Downloading from Firebase Storage requires a signed-in user !
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) { // || currentUser.uid != uid NOT FOR THE MOMENT, SINCE USER ISN'T IN AUTH DATABASE
                Log.e("FIREBASE ERROR", "User is not signed in")
                return null
            }

            firebaseStorage
                .getReference(path)
                .getFile(file)
                .await()
            return file.path

        } catch (e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            return null
        }
    }
}
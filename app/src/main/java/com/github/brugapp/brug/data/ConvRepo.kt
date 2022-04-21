package com.github.brugapp.brug.data

import android.util.Log
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

private const val USERS_DB = "Users"
private const val MSG_DB = "Messages"
private const val CONV_REFS_DB = "Conv_Refs"
private const val CONV_DB = "Conversations"

/**
 * Repository class handling bindings between the Conversation objects in Firebase & in local.
 */
object ConvRepo {
    /**
     * Adds a new Conversation between two users in Firebase, related to a given lost item.
     *
     * @param thisUID the user ID of the current user
     * @param uid the user ID of the other interlocutor
     * @param lostItemName the name of the lost item
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun addNewConversation(thisUID: String, uid: String, lostItemName: String): FirebaseResponse {
        val response = FirebaseResponse()

        try {
            //FIRST CHECK IF THE OTHER USER EXISTS OR NOT
            val userRef = Firebase.firestore.collection(USERS_DB).document(uid)
            if(!userRef.get().await().exists()){
                response.onError = Exception("User doesn't exist")
                return response
            }


            // FIRST ADD AN ENTRY IN THE CONVERSATIONS COLLECTION
            val convID = "$thisUID$uid"
            Firebase.firestore.collection(CONV_DB).document(convID).set(mapOf(
                "lost_item_name" to lostItemName
            )).await()

            // THEN ADD NEW CONV_REF ENTRY IN EACH USER'S CONV_REFS COLLECTION
            Firebase.firestore.collection(USERS_DB).document(thisUID).collection(CONV_REFS_DB).document(convID).set({}).await()
            Firebase.firestore.collection(USERS_DB).document(uid).collection(CONV_REFS_DB).document(convID).set({}).await()

            response.onSuccess = true
        } catch (e: Exception){
            response.onError = e
        }

        return response
    }

    /**
     * Deletes a given Conversation between two users from Firebase.
     *
     * @param convID the ID of the conversation to delete
     * @param thisUID the user ID of the current user
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun deleteConversationFromID(convID: String, thisUID: String): FirebaseResponse {
        val response = FirebaseResponse()

        try {
            if(parseConvUserNameFromID(convID, thisUID) == ""){
                response.onError = Exception("Conv isn't related to this user")
                return response
            }

            val convRef = Firebase.firestore.collection(CONV_DB).document(convID)
            if(!convRef.get().await().exists()){
                response.onError = Exception("Conversation doesn't exist")
                return response
            }


            val userRef = Firebase.firestore.collection(USERS_DB).document(thisUID)
            if(!userRef.get().await().exists()){
                response.onError = Exception("Conv Related User doesn't exist")
                return response
            }

            // FIRST DELETE THE CONVERSATION
            Firebase.firestore.collection(CONV_DB).document(convID).delete().await()

            // THEN REMOVE THE REFERENCE FROM THE CURRENT USER DOCUMENT IN FIREBASE
            Firebase.firestore.collection(USERS_DB).document(thisUID).collection(CONV_REFS_DB).document(convID).delete().await()

            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /* ONLY FOR TESTS */
    suspend fun deleteAllUserConversations(uid: String): FirebaseResponse {
        val response = FirebaseResponse()

        try {
            val userRef = Firebase.firestore.collection(USERS_DB).document(uid)
            if(!userRef.get().await().exists()){
                response.onError = Exception("User doesn't exist")
                return response
            }

            userRef.collection(CONV_DB).get().await().mapNotNull { conv ->
                deleteConversationFromID(conv.id, uid)
            }

            response.onSuccess = true
        } catch(e: Exception) {
            response.onError = e
        }

        return response
    }

    /**
     * Retrieves the list of Conversation of a user, given its user ID.
     *
     * @param uid the user ID
     *
     * @return List<Conversation> if the operation was successful, or a null value in case of error.
     */
    suspend fun getUserConvFromUID(uid: String): List<Conversation>? {
        return try {
            val userRef = Firebase.firestore.collection(USERS_DB).document(uid)
            if(!userRef.get().await().exists()){
                Log.e("FIREBASE ERROR", "User doesn't exist")
                return null
            }

            userRef.collection(CONV_REFS_DB).get().await().mapNotNull { convRef ->
                    getConvFromRefID(convRef.id, uid)
                }

        } catch(e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            null
        }
    }

    //TODO: ADD ERROR MESSAGES IN LOG
    private suspend fun getConvFromRefID(convID: String, authUserID: String): Conversation? {
        try {
            //FETCH CONV_DOC FROM ID
            val convSnapshot = Firebase.firestore.collection(CONV_DB).document(convID).get().await()

            // MAYBE TOO HARSH OF A CONDITION
            if(!convSnapshot.contains("lost_item_name")){
                Log.e("FIREBASE ERROR", "Invalid Conversation Format")
                return null
            }

            //FETCH USER FIELDS
            val userFields = UserRepo.getMinimalUserFromUID(
                parseConvUserNameFromID(convID, authUserID)
            ) ?: return null

            //FETCH LOSTITEMNAME
            val lostItemName = convSnapshot["lost_item_name"] as String

            //FETCH MESSAGE
            val messageUserName = userFields.getFullName()
            val messages = convSnapshot.reference.collection(MSG_DB).get().await()
                .mapNotNull { message ->
                    getMessageFromSnapshot(message, messageUserName, authUserID)
                }.sortedBy { it.timestamp.getSeconds() }
            if(messages.isEmpty()) {
                Log.e("FIREBASE ERROR", "Empty Message List")
                return null
            }

            return Conversation(convID, userFields, lostItemName, messages.toMutableList())
        } catch(e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            return null
        }
    }

    private fun getMessageFromSnapshot(snapshot: QueryDocumentSnapshot, userName: String, authUserID: String): Message? {
        if(!snapshot.contains("sender")
            || !snapshot.contains("timestamp")
            || !snapshot.contains("body")){
            Log.e("FIREBASE ERROR", "Invalid Message Format")
            return null
        }

        //TODO: CHECK IF SENDERNAME IS NOT EMPTY
        val senderName = if((snapshot["sender"] as String) != authUserID) userName else "Me"

        val message = Message(
            senderName,
            DateService.fromFirebaseTimestamp(snapshot["timestamp"] as Timestamp),
            snapshot["body"] as String,
        )

        //TODO: CLEANUP CODE A BIT MORE TO AVOID COPIES OF ATTRIBUTES
        when {
            snapshot.contains("location") -> return LocationMessage(
                message.senderName,
                message.timestamp,
                message.body,
                LocationService.fromGeoPoint(snapshot["location"] as GeoPoint)
            )
            snapshot.contains("image_url") -> return PicMessage(
                message.senderName,
                message.timestamp,
                message.body,
                snapshot["image_url"] as String
            )
            snapshot.contains("audio_url") -> return AudioMessage(
                message.senderName,
                message.timestamp,
                message.body,
                snapshot["audio_url"] as String
            )
            else -> return message
        }
    }

    private fun parseConvUserNameFromID(convID: String, uid: String): String {
        return convID.replace(uid, "", ignoreCase = false)
    }
}
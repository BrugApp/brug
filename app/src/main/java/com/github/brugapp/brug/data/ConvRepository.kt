package com.github.brugapp.brug.data

import android.util.Log
import com.github.brugapp.brug.model.Conversation
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

private const val USERS_DB = "Users"
private const val MSG_DB = "Messages"
private const val CONV_REFS_DB = "Conv_Refs"
private const val CONV_DB = "Conversations"

/**
 * Repository class handling bindings between the Conversation objects in Firebase & in local.
 */
object ConvRepository {
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
            //FIRST CHECK IF THE USERS EXIST OR NOT
            val userRef = Firebase.firestore.collection(USERS_DB).document(thisUID)
            if(!userRef.get().await().exists()){
                response.onError = Exception("User doesn't exist")
                return response
            }

            val otherUserRef = Firebase.firestore.collection(USERS_DB).document(uid)
            if(!otherUserRef.get().await().exists()){
                response.onError = Exception("Interlocutor user doesn't exist")
                return response
            }

            val convID = "$thisUID$uid"

            // CHECK IF THE CONVERSATION FOR THE OBJECT DOESN'T ALREADY EXIST
            val convDoc = Firebase.firestore.collection(CONV_DB).document(convID).get().await()
            if(convDoc.exists() && convDoc.contains("lost_item_name") && convDoc["lost_item_name"] == lostItemName){
                response.onError = Exception("The conversation for this object already exists")
                return response
            }

            // FIRST ADD AN ENTRY IN THE CONVERSATIONS COLLECTION
            Firebase.firestore.collection(CONV_DB).document(convID).set(mapOf(
                "lost_item_name" to lostItemName
            )).await()

            // THEN ADD NEW CONV_REF ENTRY IN EACH USER'S CONV_REFS COLLECTION
            userRef.collection(CONV_REFS_DB).document(convID).set({}).await()
            otherUserRef.collection(CONV_REFS_DB).document(convID).set({}).await()

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

            // awaitRealtime CHECKS FOR CHANGES RELATED TO THE LIST OF CONV_REFS,
            // AND TRIGGERS A GET REQUEST IF A CHANGE HAS BEEN DETECTED
            val convListResponse = userRef.collection(CONV_REFS_DB).awaitRealtime()
            if(convListResponse.packet != null){
                return convListResponse.packet.mapNotNull { convRef ->
                    getConvFromRefID(convRef.id, uid)
                }
            } else {
                Log.e("FIREBASE ERROR", convListResponse.error!!.message.toString())
                return listOf()
            }

        } catch(e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            null
        }
    }

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
            val userFields = UserRepository.getMinimalUserFromUID(
                parseConvUserNameFromID(convID, authUserID)
            ) ?: return null

            //FETCH LOSTITEMNAME
            val lostItemName = convSnapshot["lost_item_name"] as String

            //FETCH MESSAGES
            val messageUserName = userFields.getFullName()
            val messagesListResponse = convSnapshot.reference.collection(MSG_DB).awaitRealtime()

            val messagesList = if(messagesListResponse.packet != null){
                messagesListResponse.packet.mapNotNull { message ->
                    MessageRepository.getMessageFromSnapshot(message, messageUserName, authUserID)
                }.sortedBy { it.timestamp.getSeconds() }
            } else {
                Log.e("FIREBASE ERROR", messagesListResponse.error!!.message.toString())
                listOf()
            }

            return Conversation(convID, userFields, lostItemName, messagesList.toMutableList())
        } catch(e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            return null
        }
    }

    private fun parseConvUserNameFromID(convID: String, uid: String): String {
        return convID.replace(uid, "", ignoreCase = false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Query.awaitRealtime() = suspendCancellableCoroutine<QueryResponse> { continuation ->
        addSnapshotListener { value, error ->
            if (error == null && continuation.isActive)
                continuation.resume(QueryResponse(value, null), null)
            else if (error != null && continuation.isActive)
                continuation.resume(QueryResponse(null, error), null)
        }
    }
}
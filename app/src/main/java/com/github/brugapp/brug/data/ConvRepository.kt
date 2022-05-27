package com.github.brugapp.brug.data

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.liveData
import com.github.brugapp.brug.messaging.BrugFCMMessagingService
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import org.json.JSONArray

private const val USERS_DB = "Users"
private const val CONV_REFS_DB = "Conv_Refs"
private const val CONV_DB = "Conversations"
private const val TOKENS_DB = "Devices"

/**
 * Repository class handling bindings between the Conversation objects in Firebase & in local.
 */
object ConvRepository {
    /**
     * Adds a new Conversation between two users in Firebase, related to a given lost item.
     *
     * @param thisUID the user ID of the current user
     * @param uid the user ID of the other interlocutor
     * @param lostItemID the ID of the item belonging to a user, formatted as follows : userID:itemID
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun addNewConversation(
        thisUID: String,
        uid: String,
        lostItemID: String,
        lastMessage: Message?,
        firestore: FirebaseFirestore,
    ): FirebaseResponse {
        val response = FirebaseResponse()
        try {
            //FIRST CHECK IF THE USERS EXIST OR NOT
            val userRef = firestore.collection(USERS_DB).document(thisUID)
            val userDoc = userRef.get().await()
            if (!userDoc.exists()) {
                response.onError = Exception("User doesn't exist")
                return response
            }
            val userName = "${userDoc["first_name"] as String} ${userDoc["last_name"] as String}"

            val otherUserRef = firestore.collection(USERS_DB).document(uid)
            if (!otherUserRef.get().await().exists()) {
                response.onError = Exception("Interlocutor user doesn't exist")
                return response
            }

            val convID1 = "$thisUID$uid"
            val convID2 = "$uid$thisUID"

            // CHECK IF THE ITEM EXISTS IN THE DATABASE OR NOT
            val (userID, itemID) = lostItemID.split(":")
            val item = ItemsRepository.getSingleItemFromIDs(userID, itemID)
            if(item == null){
                response.onError = Exception("Item doesn't exist")
                return response
            }

            // CHECK IF THE CONVERSATION FOR THE OBJECT DOESN'T ALREADY EXIST (tests for both ID combinations)
            val convDoc1 = Firebase.firestore.collection(CONV_DB).document(convID1).get().await()
            val convDoc2 = Firebase.firestore.collection(CONV_DB).document(convID2).get().await()
            if((convDoc1.exists() && convDoc1.contains("lost_item_id") && convDoc1["lost_item_id"] == lostItemID)
                || (convDoc2.exists() && convDoc2.contains("lost_item_id") && convDoc2["lost_item_id"] == lostItemID)){
                response.onError = Exception("The conversation for this object already exists")
                return response
            }

            val convFields = mutableMapOf(
                "lost_item_id" to lostItemID
            )
            if(lastMessage != null){
                convFields["last_sender_id"] = lastMessage.senderName
                convFields["last_message_text"] = lastMessage.body
            }

            // FIRST ADD AN ENTRY IN THE CONVERSATIONS COLLECTION
            Firebase.firestore.collection(CONV_DB).document(convID1).set(convFields).await()

            // THEN ADD NEW CONV_REF ENTRY IN EACH USER'S CONV_REFS COLLECTION
            userRef.collection(CONV_REFS_DB).document(convID1).set({}).await()
            otherUserRef.collection(CONV_REFS_DB).document(convID1).set({}).await()


            // FINALLY SEND A NOTIFICATION TO THE USER
            val jsonArray = JSONArray(
                otherUserRef.collection(TOKENS_DB).get().await().mapNotNull { tokenDoc ->
                    tokenDoc.id
                }.toTypedArray()
            )
            BrugFCMMessagingService.sendNotificationMessage(
                jsonArray,
                "New Item Found",
                "User $userName has found your item ${item.itemName} !"
            )

            response.onSuccess = true
        } catch (e: Exception) {
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
    suspend fun deleteConversationFromID(
        convID: String,
        thisUID: String,
        firestore: FirebaseFirestore
    ): FirebaseResponse {
        val response = FirebaseResponse()

        try {
            if (parseConvUserNameFromID(convID, thisUID) == "") {
                response.onError = Exception("Conv isn't related to this user")
                return response
            }

            val convRef = firestore.collection(CONV_DB).document(convID)
            if (!convRef.get().await().exists()) {
                response.onError = Exception("Conversation doesn't exist")
                return response
            }


            val userRef = firestore.collection(USERS_DB).document(thisUID)
            if (!userRef.get().await().exists()) {
                response.onError = Exception("Conv Related User doesn't exist")
                return response
            }

            // FIRST DELETE THE CONVERSATION
            firestore.collection(CONV_DB).document(convID).delete().await()

            // THEN REMOVE THE REFERENCE FROM THE CURRENT USER DOCUMENT IN FIREBASE
            firestore.collection(USERS_DB).document(thisUID).collection(CONV_REFS_DB)
                .document(convID).delete().await()

            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /* ONLY FOR TESTS */
    suspend fun deleteAllUserConversations(
        uid: String,
        firestore: FirebaseFirestore
    ): FirebaseResponse {
        val response = FirebaseResponse()

        try {
            val userRef = firestore.collection(USERS_DB).document(uid)
            if (!userRef.get().await().exists()) {
                response.onError = Exception("User doesn't exist")
                return response
            }

            userRef.collection(CONV_DB).get().await().mapNotNull { conv ->
                deleteConversationFromID(
                    conv.id,
                    uid,
                    firestore
                )
            }

            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /**
     * Retrieves the list of messages in real-time, i.e. each time a new message is added to the conversation.
     *
     * @param uid the user ID
     *
     * @return nothing, but sets the list of conversations into the cache to be accessed by the ChatActivity if successful
     */
    fun getRealtimeConvsFromUID(
        uid: String,
        observer: LifecycleOwner,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {
        val userRef = firestore.collection(USERS_DB).document(uid)
        userRef.get().addOnCompleteListener{ task ->
            if(task.isSuccessful){
                if(task.result.exists()){
                    userRef.collection(CONV_REFS_DB).addSnapshotListener { value, error ->
                        if(value != null && error == null){
                            liveData(Dispatchers.IO){
                                emit(
                                    value.mapNotNull { convRef ->
                                        getConvFromRefID(
                                            convRef.id,
                                            uid,
                                            firestore,
                                            firebaseAuth,
                                            firebaseStorage
                                        )
                                    },
                                )
                            }.observe(observer) { list ->
                                BrugDataCache.setConversationsInCache(list.toMutableList())
                            }
                        } else {
                            Log.e("FIREBASE ERROR", error?.message.toString())
                        }
                    }
                }
            } else {
                Log.e("FIREBASE ERROR", task.exception?.message.toString())
            }
        }
    }

    private suspend fun getConvFromRefID(
        convID: String,
        authUserID: String,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): Conversation? {
        try {
            //FETCH CONV_DOC FROM ID
            val convSnapshot = firestore.collection(CONV_DB).document(convID).get().await()

            // MAYBE TOO HARSH OF A CONDITION
            if (!convSnapshot.contains("lost_item_id")) {
                Log.e("FIREBASE ERROR", "Invalid Conversation Format - Item ID not found")
                return null
            }

            //FETCH USER FIELDS
            val userFields = UserRepository.getUserFromUID(
                parseConvUserNameFromID(convID, authUserID),
                firestore,
                firebaseAuth,
                firebaseStorage
            ) ?: return null

            //FETCH LOST ITEM
            val (userID, itemID) = (convSnapshot["lost_item_id"] as String).split(":")
            val item = ItemsRepository.getSingleItemFromIDs(userID, itemID)
            if(item == null){
                Log.e("FIREBASE ERROR", "Item not found")
                return null
            }


            //FETCH INFOS RELATED TO THE LAST SENT MESSAGE (HERE TO REDUCE NUMBER OF FIREBASE QUERIES)
            val lastMessage =
                if(convSnapshot.contains("last_sender_id") && convSnapshot.contains("last_message_text")){
                    Message(
                        convSnapshot["last_sender_id"] as String,
                        DateService.fromLocalDateTime(LocalDateTime.now()),
                        convSnapshot["last_message_text"] as String
                    )
                } else null

            return Conversation(convID, userFields, item, lastMessage)
        } catch(e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            return null
        }
    }

    private fun parseConvUserNameFromID(convID: String, uid: String): String {
        return convID.replace(uid, "", ignoreCase = false)
    }
}
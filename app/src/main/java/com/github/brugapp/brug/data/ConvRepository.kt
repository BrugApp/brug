package com.github.brugapp.brug.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
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
     * @param lostItemName the name of the lost item
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun addNewConversation(
        thisUID: String,
        uid: String,
        lostItemName: String,
        firestore: FirebaseFirestore,
    ): FirebaseResponse {
        val response = FirebaseResponse()
        try {
            //FIRST CHECK IF THE USERS EXIST OR NOT
            val userRef = firestore.collection(USERS_DB).document(thisUID)
            if (!userRef.get().await().exists()) {
                response.onError = Exception("User doesn't exist")
                return response
            }

            val otherUserRef = firestore.collection(USERS_DB).document(uid)
            if (!otherUserRef.get().await().exists()) {
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
        observableList: MutableLiveData<MutableList<Conversation>>,
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
                            }.observeForever { list ->
                                observableList.postValue(list.toMutableList())
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
            if (!convSnapshot.contains("lost_item_name")) {
                Log.e("FIREBASE ERROR", "Invalid Conversation Format")
                return null
            }

            //FETCH USER FIELDS
            val userFields = UserRepository.getUserFromUID(
                parseConvUserNameFromID(convID, authUserID),
                firestore,
                firebaseAuth,
                firebaseStorage
            ) ?: return null

            //FETCH LOSTITEMNAME
            val lostItemName = convSnapshot["lost_item_name"] as String

            //FETCH INFOS RELATED TO THE LAST SENT MESSAGE (HERE TO REDUCE NUMBER OF FIREBASE QUERIES)
            val lastMessage =
                if(convSnapshot.contains("last_sender_name") && convSnapshot.contains("last_message_text")){
                    Message(
                        convSnapshot["last_sender_name"] as String,
                        DateService.fromLocalDateTime(LocalDateTime.now()),
                        convSnapshot["last_message_text"] as String
                    )
                } else null

            return Conversation(convID, userFields, lostItemName, lastMessage)
        } catch(e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            return null
        }
    }

    private fun parseConvUserNameFromID(convID: String, uid: String): String {
        return convID.replace(uid, "", ignoreCase = false)
    }
}
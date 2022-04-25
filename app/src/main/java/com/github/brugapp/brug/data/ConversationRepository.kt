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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.File

object ConversationRepository {
    private val mAuth: FirebaseAuth = Firebase.auth

    private const val USERS_DB = "Users"
    private const val MSG_DB = "Messages"
    private const val CONV_REFS_DB = "Conv_Refs"
    private const val CONV_DB = "Conversations"
    private const val ITEMS_DB = "Items"

    //adds a new message parameter to the Firestore database message collection
    fun addDocumentMessage(userID1: String, userID2: String, message: HashMap<String, String>) {
        Firebase.firestore.collection("Chat").document(userID1 + userID2).collection("Messages")
            .add(message)
    }

    /**
     * ADD MESSAGE PART
     */
    suspend fun addMessageToConv(m: Message, senderID: String, convID: String): FirebaseResponse {
        val addResponse = FirebaseResponse()
        try {
            val message: MutableMap<String, Any> = mutableMapOf(
                "sender" to senderID,
                "timestamp" to m.timestamp.toFirebaseTimestamp(),
                "body" to m.body
            )
            when (m) {
                is LocationMessage -> message["location"] = m.location.toFirebaseGeoPoint()
                is AudioMessage -> message["audio_url"] = m.audioUrl
                is PicMessage -> message["image_url"] = m.imgUrl
            }

            Firebase.firestore.collection(CONV_DB)
                .document(convID)
                .collection(MSG_DB)
                .add(message)
                .await()
            addResponse.onSuccess = true
        } catch (e: Exception) {
            addResponse.onError = e
        }
        return addResponse
    }

    //TODO: UNCOMMENT WHEN ADD CONVERSATION IS IMPLEMENTED
//    /**
//     * DELETE CONVERSATION PART
//     */
//    suspend fun deleteConvFromID(convID: String, uid: String): AddDeleteResponse{
//        val deleteResponse = AddDeleteResponse()
//        try {
//            Firebase.firestore.collection(CONV_DB).document(convID).delete().await()
//            Firebase.firestore.collection(USERS_DB).document(uid)
//                .collection(CONV_REFS_DB).document(convID).delete().await()
//            deleteResponse.onSuccess = true
//        } catch (e: Exception) {
//            deleteResponse.onError = e
//        }
//
//        return deleteResponse
//    }

    /**
     * GETTER PART
     * THE ID OF A CONVERSATION CANNOT BE NULL
     * THE USERNAME & PROFILE PIC CAN BE NULL; IN THIS CASE, A PLACEHOLDER IS LOADED & THE STRING "UNABLE TO LOAD USERNAME" IS DISPLAYED
     * THE ITEM NAME CAN BE NULL IF RETRIEVAL FAILED; IN THIS CASE, WE RETURN A STRING "UNABLE TO LOAD ITEM TYPE"
     * A LIST OF MESSAGE IN A CONVERSATION CAN CONTAIN SOME INCOMPLETE MESSAGES, BUT IT CANNOT BE EMPTY
     */

    suspend fun getConversationsFromUserID(authUserID: String): MutableList<Conversation>? {
        return try {
            Firebase.firestore.collection(USERS_DB).document(authUserID)
                .collection(CONV_REFS_DB).get().await().mapNotNull { convRef ->
                    getConvFromRefSnapshot(convRef, authUserID)
                }.toMutableList()
        } catch (e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            null
        }
    }

    private suspend fun getConvFromRefSnapshot(
        refSnapshot: QueryDocumentSnapshot,
        authUserID: String
    ): Conversation? {
        try {
            if (!refSnapshot.contains("reference")) return null

            //FETCH CONV_ID
            val convRef = refSnapshot["reference"] as DocumentReference
            val convID = convRef.id
            val convSnapshot = convRef.get().await()

            // MAYBE TOO HARSH OF A CONDITION
            if (!convSnapshot.contains("lost_item_name")) return null

            //FETCH USER FIELDS
            val userFields = ConversationRepository.getUserFieldsFromUID(
                ConversationRepository.parseConvUserNameFromID(convID, authUserID)
            ) ?: return null

            //FETCH LOSTITEMNAME
            val lostItemName = convSnapshot["lost_item_name"] as String

            //FETCH MESSAGE
            val messageUserName = userFields.getFullName()
            val messages = convSnapshot.reference.collection(MSG_DB).get().await()
                .mapNotNull { message ->
                    ConversationRepository.getMessageFromSnapshot(
                        message,
                        messageUserName,
                        authUserID
                    )
                }.sortedBy { it.timestamp.getSeconds() }
            if (messages.isEmpty()) return null

            return Conversation(convID, userFields, lostItemName, messages.toMutableList())
        } catch (e: Exception) {
            Log.e("FIREBASE CHECK", e.message.toString())
            return null
        }
    }

    private suspend fun getUserFieldsFromUID(uid: String): DummyUser? {
        try {
            val userDoc = Firebase.firestore.collection(USERS_DB).document(uid).get().await()
            if (!userDoc.contains("firstname")
                || !userDoc.contains("lastname")
            ) {
                return null
            }

            val userIcon =
                if (userDoc.contains("user_icon")) ConversationRepository.getLocalPathToUserIcon(
                    uid,
                    userDoc["user_icon"] as String
                ) else null

            //TODO: REPLACE BY CORRECT USER FORMAT
            return DummyUser(
                userDoc["firstname"] as String,
                userDoc["lastname"] as String,
                userIcon
            )
        } catch (e: Exception) {
            return DummyUser("Unknown", "Name", null)
        }
    }

    private fun parseConvUserNameFromID(convID: String, uid: String): String {
        return convID.replace(uid, "", ignoreCase = false)
    }

    private fun getMessageFromSnapshot(
        snapshot: QueryDocumentSnapshot,
        userName: String,
        authUserID: String
    ): Message? {
        if (!snapshot.contains("sender")
            || !snapshot.contains("timestamp")
            || !snapshot.contains("body")
        ) {
            return null
        }

        //TODO: CHECK IF SENDERNAME IS NOT EMPTY
        val senderName = if ((snapshot["sender"] as String) != authUserID) userName else "Me"

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

    private suspend fun getLocalPathToUserIcon(uid: String, path: String): String? {
        try {
            val file = createTempIconFileFromUserID(uid)
            // Wrapper is needed to retrieve image (due to authentication errors)
            //TODO: REPLACE ANONYMOUS AUTHENTICATION WITH CORRECT USER AUTHENTICATION
            mAuth.signInWithEmailAndPassword("unlost.app@gmail.com", "brugsdpProject1").await()
                .also {
                    Firebase.storage
                        .getReference(path)
                        .getFile(file)
                        .await()

                    mAuth.signOut()
                    return file.path
                }
        } catch (e: Exception) {
            mAuth.signOut()
            return null
        }
    }

    //REQUIRED TO MAKE THE GETUSERICONFROMPATH AN APPROPRIATE BLOCKING METHOD CALL
    private fun createTempIconFileFromUserID(uid: String): File {
        return File.createTempFile(uid, ".jpg")
    }
}
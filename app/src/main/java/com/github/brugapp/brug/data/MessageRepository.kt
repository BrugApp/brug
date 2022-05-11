package com.github.brugapp.brug.data

import android.net.Uri
import android.util.Log
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

private const val MSG_DB = "Messages"
private const val CONV_DB = "Conversations"
private const val CONV_ASSETS = "conversations_assets/"

/**
 * Repository class handling bindings between the Message objects in Firebase & in local.
 */
object MessageRepository {
    /**
     * Adds a new Message to a Conversation, given its Conversation ID.
     *
     * @param convID the conversation ID
     * @param m the message to add
     * @param senderID the ID of the sender of the message
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun addMessageToConv(
        m: Message,
        senderID: String,
        convID: String,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): FirebaseResponse {
        val addResponse = FirebaseResponse()
        try {
            val convRef = firestore.collection(CONV_DB).document(convID)
            if (!convRef.get().await().exists()) {
                addResponse.onError = Exception("Conversation doesn't exist")
                return addResponse
            }

            val message: MutableMap<String, Any> = mutableMapOf(
                "sender" to senderID,
                "timestamp" to m.timestamp.toFirebaseTimestamp(),
                "body" to m.body
            )
            when (m) {
                is LocationMessage -> message["location"] = m.location.toFirebaseGeoPoint()
                is AudioMessage -> {
                    val uploadPath =
                        uploadFileToDatabase(m.audioUrl, convID, firebaseAuth, firebaseStorage)
                    if (uploadPath.isNullOrBlank()) {
                        addResponse.onError = Exception("Unable to upload file")
                        return addResponse
                    } else {
                        message["audio_url"] = uploadPath
                    }
                }
                is PicMessage -> {
                    val uploadPath =
                        uploadFileToDatabase(m.imgUrl, convID, firebaseAuth, firebaseStorage)
                    if (uploadPath.isNullOrBlank()) {
                        addResponse.onError = Exception("Unable to upload file")
                        return addResponse
                    } else {
                        message["image_url"] = uploadPath
                    }
                }
            }

            convRef.collection(MSG_DB)
                .add(message)
                .await()
            addResponse.onSuccess = true
        } catch (e: Exception) {
            Log.d("MessageRepository", e.toString())
            addResponse.onError = e
        }
        return addResponse
    }

    suspend fun getMessageFromSnapshot(
        snapshot: QueryDocumentSnapshot,
        userName: String,
        authUserID: String,
        firebaseStorage: FirebaseStorage,
        firebaseAuth: FirebaseAuth
    ): Message? {
        if (!snapshot.contains("sender")
            || !snapshot.contains("timestamp")
            || !snapshot.contains("body")
        ) {
            Log.e("FIREBASE ERROR", "Invalid Message Format")
            return null
        }

        //TODO: CHECK IF SENDERNAME IS NOT EMPTY
        val senderName = if ((snapshot["sender"] as String) != authUserID) userName else "Me"

        val message = Message(
            senderName,
            DateService.fromFirebaseTimestamp(snapshot["timestamp"] as Timestamp),
            snapshot["body"] as String,
        )

        return when {
            snapshot.contains("location") ->
                LocationMessage.fromMessage(
                    message,
                    LocationService.fromGeoPoint(snapshot["location"] as GeoPoint)
                )

            snapshot.contains("image_url") ->
                PicMessage.fromMessage(
                    message,
                    downloadFileToTemp(
                        snapshot["image_url"] as String,
                        firebaseAuth,
                        firebaseStorage
                    ).toString()
                )

            snapshot.contains("audio_url") ->

                AudioMessage.fromMessage(message,
                    downloadFileToTemp(snapshot["audio_url"] as String, firebaseAuth, firebaseStorage).toString(),
                    downloadFileToTemp(snapshot["audio_url"] as String, firebaseAuth, firebaseStorage).toString())

            else -> TextMessage.fromMessage(message)
        }
    }


    private suspend fun uploadFileToDatabase(
        imgURI: String,
        convID: String,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): String? {
        return try {
            // Uploading to Firebase Storage requires a signed-in user !
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e("FIREBASE ERROR", "User is not signed in")
                return null
            }

            val tempSplit = imgURI.split("/")
            val filePath = "$CONV_ASSETS$convID/${tempSplit[tempSplit.size - 1]}"
            Log.d("FIREBASE CHECK", filePath)

            firebaseStorage.reference.child(filePath)
                .putFile(Uri.parse(imgURI))
                .await()

            filePath

        } catch (e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            null
        }
    }

    private suspend fun downloadFileToTemp(
        path: String,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): Uri? {
        try {
            // Downloading from Firebase Storage requires a signed-in user !
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.d("FIREBASE ERROR", "User is not signed in")
                return null
            }

            val tempSplit = path.split("/")
            val file = File.createTempFile(tempSplit[tempSplit.size - 1], ".jpg")
            Log.d("FIREBASE CHECK", file.path)

            firebaseStorage
                .getReference(path)
                .getFile(file)
                .await()

            return Uri.parse(file.path)

        } catch (e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            return null
        }
    }

}
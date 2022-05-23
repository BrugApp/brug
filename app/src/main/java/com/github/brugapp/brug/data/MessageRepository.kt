package com.github.brugapp.brug.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.liveData
import com.github.brugapp.brug.messaging.MyFCMMessagingService
import com.github.brugapp.brug.R
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.text.SimpleDateFormat
import java.util.*

private const val USERS_DB = "Users"
private const val MSG_DB = "Messages"
private const val CONV_DB = "Conversations"
private const val TOKENS_DB = "Devices"
private const val CONV_ASSETS = "conversations_assets/"
private val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRENCH)


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
        forLostNotification: Boolean,
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

            // ADD THE TEXT OF THE LAST SENT MESSAGE IN THE DOCUMENT OF THE CONVERSATION
            convRef.update(
                mapOf(
                    "last_sender_id" to senderID,
                    "last_message_text" to m.body
                )
            ).await()

            // THEN, NOTIFY THE USER THAT A NEW MESSAGE HAS BEEN SENT,
            // ONLY IF THE MESSAGE IS NOT ONE OF THOSE THAT ARE SENT
            // WHEN SCANNING THE QR CODE
            if(!forLostNotification) {
                val otherUserID = parseConvUserNameFromID(convID, senderID)
                val jsonArray = JSONArray(
                    firestore.collection(USERS_DB).document(otherUserID)
                        .collection(TOKENS_DB).get().await().mapNotNull { tokenDoc ->
                            tokenDoc.id
                        }.toTypedArray()
                )
                MyFCMMessagingService.sendNotificationMessage(jsonArray, firebaseAuth.currentUser!!.displayName, m.body)
            }


            addResponse.onSuccess = true
        } catch (e: Exception) {
            Log.d("FIREBASE ERROR - MessageRepository:", e.toString())
            addResponse.onError = e
        }
        return addResponse
    }

    /**
     * Retrieves the list of messages in real-time, i.e. each time a new message is added to the conversation,
     * given a conversation ID, the name of the interlocutor, the ID of the authenticated user and the activity which will
     * observe the values.
     *
     * @param convID the conversation ID
     * @param convUserName the name of the interlocutor
     * @param authUserID the UID of the authenticated user
     * @param context - needed to retrieve the little images displaying the location for location messages
     *
     * @return nothing, but sets the list of messages into the cache to be accessed by the ChatActivity
     */
    fun getRealtimeMessages(
        convID: String,
        convUserName: String,
        authUserID: String,
        context: Context?,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {
        firestore.collection(CONV_DB).document(convID).collection(MSG_DB).addSnapshotListener { value, error ->
            if(value != null && error == null){
                liveData(Dispatchers.IO){
                    emit(
                        value.mapNotNull { messageSnapshot ->
                            getMessageFromSnapshot(messageSnapshot, convUserName, authUserID, context, firebaseStorage, firebaseAuth)
                        }.sortedBy { it.timestamp.getSeconds() }
                    )
                }.observeForever { list ->
                    Log.e("FIREBASE STATE", "ADDING MESSAGES TO LIST")
                    BrugDataCache.setMessageListInCache(convID, list.toMutableList())
                }
            } else {
                Log.e("FIREBASE ERROR", error?.message.toString())
            }
        }
    }

    private suspend fun getMessageFromSnapshot(
        snapshot: QueryDocumentSnapshot,
        userName: String,
        authUserID: String,
        context: Context?,
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

        when {
            snapshot.contains("location") -> {
                val locationMessage = LocationMessage.fromMessage(
                    message,
                    LocationService.fromGeoPoint(snapshot["location"] as GeoPoint)
                )

                if (context != null) {
                    locationMessage.setImageUri(
                        loadImageFromUrl(
                            locationMessage.timestamp,
                            getUrlForLocation(context, locationMessage.location.toAndroidLocation())
                        )
                    )
                }
                return locationMessage
            }

            snapshot.contains("image_url") ->
                return PicMessage.fromMessage(
                    message,
                    downloadFileToTemp(
                        snapshot["image_url"] as String,
                        ".jpg",
                        firebaseAuth,
                        firebaseStorage
                    ).toString()
                )

            snapshot.contains("audio_url") -> {
                val audioFilePath = downloadFileToTemp(
                    snapshot["audio_url"] as String,
                    ".3gp",
                    firebaseAuth,
                    firebaseStorage
                ).toString()
                return AudioMessage.fromMessage(message, audioFilePath, audioFilePath)
            }

            else -> return TextMessage.fromMessage(message)
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
        suffix: String,
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
            val file = File.createTempFile(tempSplit[tempSplit.size - 1], suffix)
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

    private fun parseConvUserNameFromID(convID: String, uid: String): String {
        return convID.replace(uid, "", ignoreCase = false)
    }



    private fun getUrlForLocation(context: Context, location: Location): URL {
        val lat = location.latitude.toString()
        val lon = location.longitude.toString()
        val baseUrl =
            "https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/geojson(%7B%22type%22%3A%22Point%22%2C%22coordinates%22%3A%5B$lon%2C$lat%5D%7D)/"
        val posUrl = "$lon,$lat"
        val endUrl =
            ",15/500x500?logo=false&attribution=false&access_token=" + context.getString(R.string.mapbox_access_token)
        return URL(baseUrl + posUrl + endUrl)
    }

    private fun loadImageFromUrl(date: DateService, url: URL): Uri {
        try {
            url.openStream().use {
                val mapImgUri = File.createTempFile(simpleDateFormat.format(Date(date.getSeconds())), ".jpg")
                Channels.newChannel(it).use { rbc ->
                    FileOutputStream(mapImgUri).use { fos ->
                        fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
                    }
                }
                return Uri.fromFile(mapImgUri)
            }
        } catch (e: Exception) {
            Log.e("MAPBOX ERROR", e.message.toString())
            Log.e("MAPBOX STATIC API ERROR", "Unable to fetch image")
            return createPlaceholderLocationImage()
        }
    }

    @SuppressLint("NewApi")
    private fun createPlaceholderLocationImage(): Uri {
        val encodedImage = "iVBORw0KGgoAAAANSUhEUgAAAfQAAAH0CAIAAABEtEjdAAAFXmlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4KPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iWE1QIENvcmUgNS41LjAiPgogPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4KICA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIgogICAgeG1sbnM6ZGM9Imh0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvIgogICAgeG1sbnM6ZXhpZj0iaHR0cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iCiAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgIHhtbG5zOnBob3Rvc2hvcD0iaHR0cDovL25zLmFkb2JlLmNvbS9waG90b3Nob3AvMS4wLyIKICAgIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIKICAgIHhtbG5zOnhtcE1NPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvbW0vIgogICAgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIKICAgZXhpZjpQaXhlbFhEaW1lbnNpb249IjUwMCIKICAgZXhpZjpQaXhlbFlEaW1lbnNpb249IjUwMCIKICAgZXhpZjpDb2xvclNwYWNlPSIxIgogICB0aWZmOkltYWdlV2lkdGg9IjUwMCIKICAgdGlmZjpJbWFnZUxlbmd0aD0iNTAwIgogICB0aWZmOlJlc29sdXRpb25Vbml0PSIyIgogICB0aWZmOlhSZXNvbHV0aW9uPSI3Mi8xIgogICB0aWZmOllSZXNvbHV0aW9uPSI3Mi8xIgogICBwaG90b3Nob3A6Q29sb3JNb2RlPSIzIgogICBwaG90b3Nob3A6SUNDUHJv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"
        val decodedImage = Base64.getDecoder().decode(encodedImage)
        val image = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)

        // store to outputstream
        val tempFile = File.createTempFile(simpleDateFormat.format(Date()), ".jpg")
        val outputStream = FileOutputStream(tempFile)
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return Uri.fromFile(tempFile)
    }
}
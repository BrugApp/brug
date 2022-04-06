package com.github.brugapp.brug.data

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.File

private const val USERS_DB = "Users"
private const val MSG_DB = "Messages"
private const val CONV_REFS_DB = "Conv_Refs"
private const val CONV_DB = "Conversations"
private const val ITEMS_DB = "Items"

object FirebaseHelper {

    private val mAuth: FirebaseAuth = Firebase.auth

    //returns Firestore authentication
    fun getFirebaseAuth(): FirebaseAuth {
        return mAuth
    }

    //returns the current session's authenticated user
    fun getCurrentUser(): FirebaseUser? {
        return mAuth.currentUser
    }

    //returns the unique user ID for the current user
    fun getCurrentUserID(): String? {
        return getCurrentUser()?.uid
    }

    //returns the collection of users from the Firestore database
    fun getUserCollection(): CollectionReference {
        return Firebase.firestore.collection("Users")
    }

    //returns the collection of chats from the Firestore database
    fun getChatCollection(): CollectionReference {
        return Firebase.firestore.collection("Chat")
    }

    //returns the specific chat document between userID1 and userID2
    fun getChatFromIDPair(userID1: String, userID2: String): DocumentReference {
        return getChatCollection().document(userID1 + userID2)
    }

    //returns the collection of messages from a specific chat between userID1 and userID2
    fun getMessageCollection(userID1: String, userID2: String): CollectionReference {
        return getChatFromIDPair(userID1, userID2).collection("Messages")
    }

    //adds a new user parameter to the Firestore database user collection
    fun addRegisterUserTask(userToAdd: HashMap<String, Any>): Task<DocumentReference> {
        return getUserCollection().add(userToAdd)
    }


//    //TODO: REFACTOR USER CLASS TO HOLD THE NECESSARY VALUES
//    suspend fun getCompleteUserFromID(uid: String): UserResponse {
//        val userResponse = UserResponse()
//        try {
//            val userDoc = db.collection(USERS_DB).document(uid)
//            val retrievedUserDoc = userDoc.get().await()
//
//
//            if(!retrievedUserDoc.exists()){
//                userResponse.onError = Exception("The requested user doesn't exist")
//            } else if(!retrievedUserDoc.contains("firstname")
//                || !retrievedUserDoc.contains("lastname")
//                || !retrievedUserDoc.contains("user_icon")
//                || !retrievedUserDoc.contains("conversations")) {
//                userResponse.onError = Exception("Invalid User format")
//            } else {
//                val firstname = retrievedUserDoc["firstname"] as String
//                val lastname = retrievedUserDoc["lastname"] as String
//                val userIconPath = retrievedUserDoc["user_icon"] as String
//                val userItems = userDoc.collection(ITEMS_DB).get().await().map { item ->
//                    getUserItemFromSnapshot(item)
//                }
//                val userConvs = userDoc.collection(CONV_REFS_DB).get().await().map{ conversation ->
//                    getUserConvFromSnapshot(conversation, userDoc.id)
//                }
//                userResponse.onSuccess = User(firstname, lastname, userIconPath, userItems.toMutableList(), userConvs.toMutableList())
//            }
//        } catch (e: Exception){
//            userResponse.onError = e
//        }
//        return userResponse
//    }
//
//
//    //TODO: REFACTOR ITEM CLASS TO HOLD ISLOST IN PARAMETERS
//    private suspend fun getUserItemFromSnapshot(item: QueryDocumentSnapshot): ItemResponse {
//        val itemResponse = ItemResponse()
//        try {
//            if(!item.contains("item_type")
//                || !item.contains("item_description")
//                || !item.contains("is_lost")){
//                itemResponse.onError = Exception("Invalid Item format")
//            } else {
//                val itemTypePair = getItemNameFromPath(item["item_type"] as String)
//                val itemDesc = item["item_description"] as String
//                val isLostFlag = item["is_lost"] as Boolean
//                itemResponse.onSuccess = Item(itemTypePair, itemDesc, isLostFlag)
//            }
//        } catch (e: Exception) {
//            itemResponse.onError = e
//        }
//        return itemResponse
//    }
//


    /**
     * ADD MESSAGE PART
     */
    suspend fun addMessageToConv(m: Message, senderID: String, convID: String): AddDeleteResponse {
        val addResponse = AddDeleteResponse()
        try {
            val message = mutableMapOf(
                "sender" to Firebase.firestore.document("$USERS_DB/$senderID"),
                "timestamp" to m.timestamp.toFirebaseTimestamp(),
                "body" to m.body
            )
            when(m){
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
        } catch(e: Exception){
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
     */
    //TODO: REMOVE THIS FUNCTION WHEN USER RETRIEVAL IS COMPLETED
    suspend fun getConversationsFromUserID(uid: String): TempConvListResponse {
        val tempConvListResponse = TempConvListResponse()
        try {
            val convSnapshot = Firebase.firestore.collection(USERS_DB).document(uid).collection(CONV_REFS_DB).get().await().map { conv ->
                if(!conv.contains("reference")){
                    Log.e("Firebase error", "No reference found")
                } else {
                    getUserConvFromRef(conv["reference"] as DocumentReference, uid)
                }
            }
            tempConvListResponse.onSuccess = convSnapshot as List<ConvResponse>
        } catch(e: Exception) {
            tempConvListResponse.onError = e
        }
        return tempConvListResponse
    }


    private suspend fun getUserConvFromRef(ref: DocumentReference, uid: String): ConvResponse {
        val convResponse = ConvResponse()
        try{
            val conv = ref.get().await()
            if(!conv.contains("lost_item_path")){
                convResponse.onError = Exception("Invalid Conversation format")
            } else {
                val convID = conv.id
                val convUserFields = getUserFieldsFromRef(
                    Firebase.firestore
                        .collection(USERS_DB)
                        .document(parseConvUserNameFromID(conv.id, uid)))
                val lostItemName = getLostItemNameFromRef(conv["lost_item_path"] as DocumentReference)
                val messages = conv.reference.collection(MSG_DB).get().await().map { message ->
                    getConvMessageFromSnapshot(message)
                }.sortedBy { it.onSuccess?.timestamp?.getSeconds() }

                convResponse.onSuccess = Conversation(convID, convUserFields, lostItemName, messages.toMutableList())
            }

        } catch (e: Exception) {
            convResponse.onError = e
        }
        return convResponse
    }

    private fun parseConvUserNameFromID(convID: String, uid: String): String {
        return convID.replace(uid, "", ignoreCase = false)
    }

    // MAYBE CONSIDER REFACTORING
    private suspend fun getLostItemNameFromRef(ref: DocumentReference): ItemNameResponse {
        val stringResponse = ItemNameResponse()
        try {
            val itemDoc = ref.get().await()
            if(!itemDoc.exists()){
                stringResponse.onError = Exception("The requested item doesn't exist")
            } else if (!itemDoc.contains("item_type")) {
                stringResponse.onError = Exception("Invalid Item format")
            } else {
                val getItemName = getItemTypeFromRef(itemDoc["item_type"] as DocumentReference)
                if(getItemName.onError != null){
                    stringResponse.onError = getItemName.onError
                } else {
                    stringResponse.onSuccess = getItemName.onSuccess!!.first
                }
            }
        } catch(e: Exception){
            stringResponse.onError = e
        }

        return stringResponse
    }

    // NEEDED FUNCTION TO RETRIEVE ITEM TYPE FIELDS
    private suspend fun getItemTypeFromRef(ref: DocumentReference): ItemTypeResponse {
        val stringPairResponse = ItemTypeResponse()
        try {
            val itemTypeDoc = ref.get().await()
            if(!itemTypeDoc.exists()){
                stringPairResponse.onError = Exception("The requested item type doesn't exist")
            } else if(
                !itemTypeDoc.contains("type_icon")
                || !itemTypeDoc.contains("type_name")) {
                stringPairResponse.onError = Exception("Invalid Item Type format")
            } else {
                val itemName = itemTypeDoc["type_name"] as String
                val itemPicPath = itemTypeDoc["type_icon"] as String
                stringPairResponse.onSuccess = Pair(itemName, itemPicPath)
            }
        } catch(e: Exception){
            stringPairResponse.onError = e
        }
        return stringPairResponse
    }

    private suspend fun getConvMessageFromSnapshot(messageDoc: QueryDocumentSnapshot): MessageResponse {
        val messageResponse = MessageResponse()
        try {
            if (!messageDoc.contains("sender")
                || !messageDoc.contains("timestamp")
                || !messageDoc.contains("body")
            ) {
                messageResponse.onError = Exception("Invalid message format")

            } else {
                val senderResponse = getUserFieldsFromRef(messageDoc["sender"] as DocumentReference)
                if (senderResponse.onError != null){
                    messageResponse.onError = senderResponse.onError
                } else {
                    val timeStamp = messageDoc["timestamp"] as Timestamp
                    val body = messageDoc["body"] as String

                    val message: Message
                    when {
                        messageDoc.contains("location") -> {
                            val location = messageDoc["location"] as GeoPoint

                            message = LocationMessage(
                                senderResponse.onSuccess!!.first,
                                DateService.fromFirebaseTimestamp(timeStamp),
                                body,
                                LocationService.fromGeoPoint(location))
                        }
                        messageDoc.contains("image_url") -> {
                            val imgUrl = messageDoc["image_url"] as String
                            message = PicMessage(
                                senderResponse.onSuccess!!.first,
                                DateService.fromFirebaseTimestamp(timeStamp),
                                body,
                                imgUrl)
                        }
                        messageDoc.contains("audio_url") -> {
                            //TODO: COMPLETE THIS FUNCTION WITH CORRECT RETRIEVAL OF AUDIO FILE FROM FIRESTORE
                            val audioUrl = messageDoc["audio_url"] as String
                            message = AudioMessage(
                                senderResponse.onSuccess!!.first,
                                DateService.fromFirebaseTimestamp(timeStamp),
                                body,
                                audioUrl)
                        }
                        else -> {
                            message = Message(
                                senderResponse.onSuccess!!.first,
                                DateService.fromFirebaseTimestamp(timeStamp),
                                body)
                        }
                    }
                    messageResponse.onSuccess = message
                }
            }
        } catch(e: Exception){
            messageResponse.onError = e
        }
        return messageResponse
    }

    private suspend fun getUserFieldsFromRef(ref: DocumentReference): UserFieldsResponse {
        val convUserResponse = UserFieldsResponse()
        try {
            val userDoc = ref.get().await()
            if(!userDoc.exists()){
                convUserResponse.onError = Exception("The requested user doesn't exist")
            } else {
                if(!userDoc.contains("firstname")
                    || !userDoc.contains("lastname")
                    || !userDoc.contains("user_icon")){
                    convUserResponse.onError = Exception("Invalid user format")
                } else {
                    convUserResponse.onSuccess = Pair(
                        "${userDoc["firstname"] as String} ${userDoc["lastname"] as String}",
                        downloadUserIconFromPath(userDoc.id, (userDoc["user_icon"] as String))
                    )
                }
            }
        } catch (e: Exception){
            convUserResponse.onError = e
        }
        return convUserResponse
    }

    private suspend fun downloadUserIconFromPath(uid: String, path: String): FileResponse {
        val fileResponse = FileResponse()
        try {
            val file = createTempIconFileFromUserID(uid)
            // Wrapper is needed to retrieve image (due to authentication errors)
            //TODO: REPLACE ANONYMOUS AUTHENTICATION WITH CORRECT USER AUTHENTICATION
            mAuth.signInAnonymously().await().also {
                Firebase.storage
                    .getReference(path)
                    .getFile(file)
                    .await()

                fileResponse.onSuccess = file
            }
        } catch (e: Exception) {
            fileResponse.onError = e
        }

        return fileResponse
    }

    //REQUIRED TO MAKE THE GETUSERICONFROMPATH AN APPROPRIATE BLOCKING METHOD CALL
    private fun createTempIconFileFromUserID(uid: String): File {
        return File.createTempFile(uid, ".jpg")
    }





























    //adds a new message parameter to the Firestore database message collection
    fun addDocumentMessage(
        userID1: String,
        userID2: String,
        message: HashMap<String, String>
    ): Task<DocumentReference> {
        return getMessageCollection(userID1, userID2).add(message)
    }

    /*
    Returns a User HashMap object that can be sent to FireBase
    @param  emailtxt  the email of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return the User HashMap object with given parameters that can be sent to firebase
    */
    fun createNewRegisterUser(emailtxt: String, firstnametxt: String, lastnametxt: String): HashMap<String, Any> {
        val user = getCurrentUser()
        val list = listOf<String>()
        val userToAdd = hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (user?.uid ?: String),
            "email" to emailtxt,
            "firstName" to firstnametxt,
            "lastName" to lastnametxt
        )
        return userToAdd
    }

    /*
    Returns a Task<DocumentReference> that tries to create a new FireBase User
    Toasts text corresponding to the task's success/failure
    @param  emailtxt  the email of the user that we are building
    @param  passwordtxt the password of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return the Task<DocumentReference> that tries to create a new FireBase User
    */
    fun createAuthAccount(context: Context, progressBar: ProgressBar, emailtxt: String, passwordtxt: String, firstnametxt: String, lastnametxt: String){
        mAuth.createUserWithEmailAndPassword(emailtxt, passwordtxt)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    addRegisterUserTask(createNewRegisterUser(emailtxt, firstnametxt, lastnametxt))
                        .addOnSuccessListener { documentReference ->
                            Log.d(
                                ContentValues.TAG,
                                "DocumentSnapshot added with ID: ${documentReference.id}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                ContentValues.TAG,
                                "Error adding document",
                                e
                            )
                        }
                    progressBar.visibility = View.GONE
                } else { // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                }
            }
    }
}
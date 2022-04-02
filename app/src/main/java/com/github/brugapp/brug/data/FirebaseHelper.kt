package com.github.brugapp.brug.data

import android.content.ContentValues
import android.content.Context
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.github.brugapp.brug.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

private const val USERS_DB = "Users"
private const val MSG_DB = "Messages"
private const val CONV_REFS_DB = "Conv_Refs"
private const val ITEMS_DB = "Items"

object FirebaseHelper {

    private val db: FirebaseFirestore = Firebase.firestore
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
        return db.collection("Users")
    }

    //returns the collection of chats from the Firestore database
    fun getChatCollection(): CollectionReference {
        return db.collection("Chat")
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


    //TODO: REFACTOR USER CLASS TO HOLD THE NECESSARY VALUES
    suspend fun getAuthUserFieldsFromID(uid: String): UserResponse {
        val userResponse = UserResponse()
        try {
            val userDoc = db.collection(USERS_DB).document(uid)
            val retrievedUserDoc = userDoc.get().await()


            if(!retrievedUserDoc.exists()){
                userResponse.onError = Exception("The requested user doesn't exist")
            } else if(!retrievedUserDoc.contains("firstname")
                || !retrievedUserDoc.contains("lastname")
                || !retrievedUserDoc.contains("user_icon")
                || !retrievedUserDoc.contains("conversations")) {
                userResponse.onError = Exception("Invalid User format")
            } else {
                val firstname = retrievedUserDoc["firstname"] as String
                val lastname = retrievedUserDoc["lastname"] as String
                val userIconPath = retrievedUserDoc["user_icon"] as String
                val userItems = userDoc.collection(ITEMS_DB).get().await().map { item ->
                    getUserItemFromSnapshot(item)
                }
                val userConvs = userDoc.collection(CONV_REFS_DB).get().await().map{ conversation ->
                    getUserConvFromSnapshot(conversation, userDoc.id)
                }
                userResponse.onSuccess = User(firstname, lastname, userIconPath, userItems, userConvs)
            }
        } catch (e: Exception){
            userResponse.onError = e
        }
        return userResponse
    }


    //TODO: REFACTOR ITEM CLASS TO HOLD ISLOST IN PARAMETERS
    private suspend fun getUserItemFromSnapshot(item: QueryDocumentSnapshot): ItemResponse {
        val itemResponse = ItemResponse()
        try {
            if(!item.contains("item_name")
                || !item.contains("item_type")
                || !item.contains("item_description")
                || !item.contains("is_lost")){
                itemResponse.onError = Exception("Invalid Item format")
            } else {
                val itemName = item["item_name"] as String
                val itemTypePath = item["item_type"] as String
                val itemDesc = item["item_description"] as String
                val isLostFlag = item["is_lost"] as Boolean
                itemResponse.onSuccess = Item(itemName, itemDesc, itemTypePath, isLostFlag)
            }
        } catch (e: Exception) {
            itemResponse.onError = e
        }
        return itemResponse
    }


    private suspend fun getUserConvFromSnapshot(conv: QueryDocumentSnapshot, uid: String): ConvResponse {
        val convResponse = ConvResponse()
        try{
            if(!conv.contains("lost_item_path")){
                convResponse.onError = Exception("Invalid Conversation format")
            } else {
                val convUserID = getUserNameFromPath(getConvUserNameFromID("$USERS_DB/${conv.id}", uid))
                val lostItemName = getLostItemNameFromRef(conv["lost_item_path"] as String)
                val messages = conv.reference.collection(MSG_DB).get().await().map { message -> //NEED TO CHECK IF CORRECT
                    getConvMessageFromSnapshot(message)
                }

                convResponse.onSuccess = Conversation(convUserID, lostItemName, messages.toMutableList())
            }

        } catch (e: Exception) {
            convResponse.onError = e
        }
        return convResponse
    }

    private fun getConvUserNameFromID(convID: String, uid: String): String {
        return convID.replace(uid, "", ignoreCase = false)
    }

    private suspend fun getLostItemNameFromRef(ref: String): StringResponse {
        val stringResponse = StringResponse()
        try {
            val itemDoc = db.document(ref).get().await()
            if(!itemDoc.exists()){
                stringResponse.onError = Exception("The requested item doesn't exist")
            } else if (!itemDoc.contains("item_name")) {
                stringResponse.onError = Exception("Invalid Item format")
            } else {
                stringResponse.onSuccess = itemDoc["item_name"] as String
            }
        } catch(e: Exception){
            stringResponse.onError = e
        }

        return stringResponse
    }


    //TODO: REFACTOR MESSAGE CLASS TO REMOVE MID
    //TODO: HAVE MANY MESSAGE TYPES IMPLEMENTATIONS
    private suspend fun getConvMessageFromSnapshot(messageDoc: QueryDocumentSnapshot): MessageResponse {
        val messageResponse = MessageResponse()
        try {
            if (!messageDoc.contains("sender")
                || !messageDoc.contains("timestamp")
                || !messageDoc.contains("body")
            ) {
                messageResponse.onError = Exception("Invalid message format")
            } else {
                val senderNameResponse = getUserNameFromPath(messageDoc["sender"] as String)
                if (senderNameResponse.onError != null){
                    messageResponse.onError = senderNameResponse.onError
                } else {
                    val timeStamp = messageDoc["timestamp"] as LocalDateTime
                    val body = messageDoc["body"] as String

                    val message: Message
                    when {
                        messageDoc.contains("location") -> {
                            //TODO: PROPERLY HANDLE LOCATION MESSAGES
                            val location = messageDoc["location"] as Location
                            message = LocationMessage(senderNameResponse.onSuccess!!, timeStamp, body, location)
                        }
                        messageDoc.contains("image_url") -> {
                            //TODO: PROPERLY HANDLE IMAGE MESSAGES
                            val imgUrl = messageDoc["image_url"] as String
                            message = PicMessage(senderNameResponse.onSuccess!!, timeStamp, body, imgUrl)
                        }
                        messageDoc.contains("audio_url") -> {
                            //TODO: PROPERLY HANDLE AUDIO MESSAGES
                            val audioUrl = messageDoc["audio_url"] as String
                            message = AudioMessage(senderNameResponse.onSuccess!!, timeStamp, body, audioUrl)
                        }
                        else -> {
                            message = Message(senderNameResponse.onSuccess!!, timeStamp, body)
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

    private suspend fun getUserNameFromPath(path: String): StringResponse {
        val stringResponse = StringResponse()
        try {
            val userDoc = db.document(path).get().await()
            if(!userDoc.exists()){
                stringResponse.onError = Exception("The requested user doesn't exist")
            } else {
                if(userDoc.contains("firstname") && userDoc.contains("lastname")){
                    stringResponse.onSuccess = "${userDoc["firstname"] as String} ${userDoc["lastname"] as String}"
                } else {
                    stringResponse.onError = Exception("Invalid user format")
                }
            }
        } catch (e: Exception){
            stringResponse.onError = e
        }
        return stringResponse
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
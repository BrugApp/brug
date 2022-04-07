package com.github.brugapp.brug.data

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.User
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
    fun getCurrentUser(userID: String): User? {
        lateinit var firstname: String
        lateinit var lastname: String
        //lateinit var Conv_Refs: MutableList<String>
        //lateinit var Items: MutableList<Item>

        if (mAuth.currentUser != null) {
            val docRef = Firebase.firestore.collection("Users").document(userID)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    if (document.data?.get("firstname") != null && document.data?.get("lastname") != null) {
                        firstname = document.data?.get("firstname") as String
                        lastname = document.data?.get("lastname") as String
                    }
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
            val email = mAuth.currentUser!!.email
            val id = mAuth.uid
            val uri = mAuth.currentUser!!.photoUrl
            var inputStream : Uri? = null
            var profilePicture: Drawable? = null

            if (email == null || id == null || uri == null) {
                return null
            } else {
                return User(firstname, lastname, email, id, profilePicture)
            }
            //items & conv_ref attributes will be added here later
        }
        return null
    }
    //returns item from a given user
    fun getItemFromCurrentUser(userID: String, objectID: String): Item? {
        lateinit var name: String
        lateinit var description: String
        var is_lost = false
        var success = false
        lateinit var item_ref: DocumentReference
        lateinit var item_type: String
        val docRef = Firebase.firestore.collection("Users").document(userID).collection("Items").document(objectID)
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                if(document.data?.get("is_lost")!=null && document.data?.get("item_description")!=null && document.data?.get("item_type")!=null) {
                    is_lost = document.data?.get("is_lost") as Boolean
                    description = document.data?.get("item_description") as String
                    item_ref = document.data?.get("item_type") as DocumentReference
                    item_type = item_ref.toString().drop(item_ref.toString().length-1)
                    success = true
                }else{
                    is_lost = false
                    description = ""
                    item_type = ""
                }
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
        if (success) {
            val item = Item(name, description, objectID)
            //item.setType(item_type)
            //@TODO convert item_type string to item_type local type
            item.setLost(is_lost)
            return item
        } else {
            return null
        }
    }
    //@TODO functionA for person1 to declare item1 lost
    //@TODO functionB for person2 to declare person1's item1 as found(unlost), creates chat (p1,p2)

    //adds a new user parameter to the Firestore database user collection
    fun addRegisterUser(userToAdd: HashMap<String, Any>) {
        Firebase.firestore.collection("Users").add(userToAdd)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    //adds a new message parameter to the Firestore database message collection
    fun addDocumentMessage(userID1: String, userID2: String, message: HashMap<String, String>) {
        Firebase.firestore.collection("Chat").document(userID1 + userID2).collection("Messages").add(message)
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
        } catch(e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            null
        }
    }

    private suspend fun getConvFromRefSnapshot(refSnapshot: QueryDocumentSnapshot, authUserID: String): Conversation? {
        try {
            if(!refSnapshot.contains("reference")) return null

            //FETCH CONV_ID
            val convRef = refSnapshot["reference"] as DocumentReference
            val convID = convRef.id
            val convSnapshot = convRef.get().await()

            // MAYBE TOO HARSH OF A CONDITION
            if(!convSnapshot.contains("lost_item_name")) return null

            //FETCH USER FIELDS
            val userFields = getUserFieldsFromUID(
                parseConvUserNameFromID(convID, authUserID)) ?: return null

            //FETCH LOSTITEMNAME
            val lostItemName = convSnapshot["lost_item_name"] as String

            //FETCH MESSAGE
            val messageUserName = userFields.getFullName()
            val messages = convSnapshot.reference.collection(MSG_DB).get().await()
                .mapNotNull { message ->
                    getMessageFromSnapshot(message, messageUserName, authUserID)
                }.sortedBy { it.timestamp.getSeconds() }
            if(messages.isEmpty()) return null

            return Conversation(convID, userFields, lostItemName, messages.toMutableList())
        } catch(e: Exception) {
            Log.e("FIREBASE CHECK", e.message.toString())
            return null
        }
    }


    private suspend fun getUserFieldsFromUID(uid: String): DummyUser? {
        try {
            val userDoc = Firebase.firestore.collection(USERS_DB).document(uid).get().await()
            if(!userDoc.contains("firstname")
                || !userDoc.contains("lastname")){
                return null
            }

            val userIcon = if(userDoc.contains("user_icon")) getLocalPathToUserIcon(uid, userDoc["user_icon"] as String) else null

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

    private suspend fun getLocalPathToUserIcon(uid: String, path: String): String? {
        try {
            val file = createTempIconFileFromUserID(uid)
            // Wrapper is needed to retrieve image (due to authentication errors)
            //TODO: REPLACE ANONYMOUS AUTHENTICATION WITH CORRECT USER AUTHENTICATION
            mAuth.signInWithEmailAndPassword("unlost.app@gmail.com", "brugsdpProject1").await().also {
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

    private fun parseConvUserNameFromID(convID: String, uid: String): String {
        return convID.replace(uid, "", ignoreCase = false)
    }


    private fun getMessageFromSnapshot(snapshot: QueryDocumentSnapshot, userName: String, authUserID: String): Message? {
        if(!snapshot.contains("sender")
            || !snapshot.contains("timestamp")
            || !snapshot.contains("body")){
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




    /*
    Returns a User HashMap object that can be sent to FireBase
    @param  emailtxt  the email of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return the User HashMap object with given parameters that can be sent to firebase
    */
    fun createNewRegisterUser(
        emailtxt: String,
        firstnametxt: String,
        lastnametxt: String
    ): HashMap<String, Any> {
        val list = listOf<String>()
        val userToAdd = hashMapOf(
            "ItemIDArray" to list,
            "UserID" to (mAuth.uid ?: String),
            "email" to emailtxt,
            "firstName" to firstnametxt,
            "lastName" to lastnametxt
        )
        return userToAdd
    }

    /*
    Returns void after executing a Task<DocumentReference> that tries to create a new FireBase User
    Toasts text corresponding to the task's success/failure
    @param  emailtxt  the email of the user that we are building
    @param  passwordtxt the password of the user that we are building
    @param  firstnametxt  the first name of the user that we are building
    @param  lastnametxt the last name of the user that we are building
    @return void after execution of the Task<DocumentReference> that tries to create a new FireBase User
    */
    fun createAuthAccount(
        context: Context,
        progressBar: ProgressBar,
        emailtxt: String,
        passwordtxt: String,
        firstnametxt: String,
        lastnametxt: String
    ) {
        mAuth.createUserWithEmailAndPassword(emailtxt, passwordtxt)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    addRegisterUser(createNewRegisterUser(emailtxt, firstnametxt, lastnametxt))
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

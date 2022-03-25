package com.github.brugapp.brug.view_model

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime

class ChatViewModel : ViewModel() {
    private lateinit var adapter: ChatMessagesListAdapter

    fun initViewModel(messages: MutableList<ChatMessage>) {
        this.messages = messages
        this.adapter = ChatMessagesListAdapter(messages)
    }

    fun getAdapter(): ChatMessagesListAdapter {
        return adapter
    }

    fun requestLocation(activity: Activity, fusedLocationClient: FusedLocationProviderClient, locationManager: LocationManager) {
        if(ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                locationRequestCode)
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation: Location? ->
            if (lastKnownLocation != null) {
                sendLocation(lastKnownLocation)
            } else {
                // Launch the locationListener (updates every 1000 ms)
                val locationGpsProvider = LocationManager.GPS_PROVIDER
                locationManager.requestLocationUpdates(
                    locationGpsProvider,
                    50,
                    0.1f
                ) { sendLocation(it) }
            }
        }
    }

    private fun sendLocation(location: Location){
        val locationString = "longitude: ${location.longitude}; latitude: ${location.latitude}"
        val newMessage = ChatMessage(locationString, LocalDateTime.now(), "Location")
        messages.add(newMessage)
        adapter.notifyItemInserted(messages.size-1)
//        sendMessage(locationString)
    }

        // Add a new document i.e. localisation
        helper.addDocumentMessage("UserID1","UserID2",message)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }
}


//
//    private fun foundLocation(location: Location) {
//        sendLocalisation(location.longitude, location.latitude)
//    }


//    fun sendLocalisation(longitude: Double, latitude: Double) {
//        // TODO: Change the document when ChatListActivity is implemented
//        // TODO: Update code to use data.Database when implemented
//        // Get localisation of user
//        val localisation: String = "longitude: $longitude; latitude: $latitude"
//
//        // Compute datetime
//        val datetime: String = computeDateTime()
//
//        // Create a message
//        val message = hashMapOf(
//            "sender" to "Localisation service",
//            "content" to localisation,
//            "datetime" to datetime
//        )
//
//        // Add a new document i.e. localisation
//        db = Firebase.firestore
//        db.collection("Chat").document("User1User2")
//            .collection("Messages")
//            .add(message)
//            .addOnSuccessListener { documentReference ->
//                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
//            }
//            .addOnFailureListener { e ->
//                Log.w(ContentValues.TAG, "Error adding document", e)
//            }
//    }
}


// OLD IMPLEMENTATION USING FIREBASE -> TO BE REFACTORED PROPERLY AFTERWARDS
// // TODO: Have to be removed when the data.DataBase class is implemented
// private lateinit var db: FirebaseFirestore
//
// private lateinit var chatArrayList: ArrayList<ChatMessage>
// private lateinit var adapter: ChatMessagesListAdapter
//
// fun initAdapter() {
// chatArrayList = arrayListOf()
// adapter = ChatMessagesListAdapter(chatArrayList)
// }
//
// fun getAdapter(): ChatMessagesListAdapter {
// return adapter
// }
//
// fun eventChangeListener(activity: ChatActivity) {
// // TODO: Change the document when ChatListActivity is implemented
// // TODO: Update code to use data.Database when implemented
// db = Firebase.firestore
// db.collection("Chat").document("User1User2").collection("Messages")
// .orderBy("datetime", Query.Direction.ASCENDING)
// .addSnapshotListener(object : EventListener<QuerySnapshot> {
// @SuppressLint("NotifyDataSetChanged") // Used by the adapter
// override fun onEvent(
// value: QuerySnapshot?,
// error: FirebaseFirestoreException?
// ) {
// if (error != null) {
// Log.e("Firestore error", error.message.toString())
// return
// }
//
// // Add retrieved messages to the list of displayed messages
// for (dc: DocumentChange in value?.documentChanges!!)
// if (dc.type == DocumentChange.Type.ADDED) {
// chatArrayList.add(dc.document.toObject(ChatMessage::class.java))
// }
//
// // Notify the adapter to update the list
// adapter.notifyDataSetChanged()
// // TODO: Would be cleaner to have this update inside of ChatActivity
// //activity.updateData(adapter.itemCount - 1)
// // Instead I have to do this
// val rv = activity.findViewById<View>(R.id.recyclerView) as RecyclerView
// rv.smoothScrollToPosition(adapter.itemCount - 1)
// }
// })
// }
//
// @RequiresApi(Build.VERSION_CODES.O) // Required for datetime
// fun sendMessage(sender: String, content: String) {
// // TODO: Change the document when ChatListActivity is implemented
// // TODO: Update code to use data.Database when implemented
// // Compute timestamp
// val datetime: String = computeDateTime()
//
// // Create a new message
// val message = hashMapOf(
// "sender" to sender,
// "content" to content,
// "datetime" to datetime
// )
//
// // Add a new document i.e. message
// db = Firebase.firestore
// db.collection("Chat").document("User1User2")
// .collection("Messages")
// .add(message)
// .addOnSuccessListener { documentReference ->
// Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
// }
// .addOnFailureListener { e ->
// Log.w(ContentValues.TAG, "Error adding document", e)
// }
// }
//
// @RequiresApi(Build.VERSION_CODES.O)
// private fun computeDateTime(): String {
// val current = LocalDateTime.now()
// val formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss")
// return current.format(formatter)
// }
//
// @RequiresApi(Build.VERSION_CODES.O) // Required for datetime
// fun sendLocalisation(longitude: Double, latitude: Double) {
// // TODO: Change the document when ChatListActivity is implemented
// // TODO: Update code to use data.Database when implemented
// // Get localisation of user
// val localisation: String
// localisation = "longitude: $longitude; latitude: $latitude"
//
// // Compute datetime
// val datetime: String = computeDateTime()
//
// // Create a message
// val message = hashMapOf(
// "sender" to "Localisation service",
// "content" to localisation,
// "datetime" to datetime
// )
//
// // Add a new document i.e. localisation
// db = Firebase.firestore
// db.collection("Chat").document("User1User2")
// .collection("Messages")
// .add(message)
// .addOnSuccessListener { documentReference ->
// Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
// }
// .addOnFailureListener { e ->
// Log.w(ContentValues.TAG, "Error adding document", e)
// }
// }
// }
>>>>>>> 5cd596ab4a3dee32f328655bd25d46e9df604f9b

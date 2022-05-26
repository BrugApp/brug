
package com.github.brugapp.brug.data

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.liveData
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await

private const val USERS_DB = "Users"
private const val ITEMS_DB = "Items"

/**
 * Repository class handling bindings between the Item objects in Firebase & in local.
 */
object ItemsRepository {
    /**
     * Adds a new item to a Firebase user, given a user ID.
     *
     * @param item the item to add
     * @param uid the user ID
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun addItemToUser(
        item: Item,
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

            val map = mutableMapOf<String, Any>(
                "item_name" to item.itemName,
                "item_type" to item.getItemTypeID(),
                "item_description" to item.itemDesc,
                "is_lost" to item.isLost(),
            )
            if(item.getLastLocation() != null){
                map["last_location"] = item.getLastLocation()!!.toFirebaseGeoPoint()
            }

            userRef.collection(ITEMS_DB).add(map).await()

            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /**
     * Sets the last location of an item.
     *
     * @param uid the user ID of the owner of the item
     * @param itemID the ID of the item
     * @param lastLocation the last location of the item
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun addLastLocation(
        uid: String,
        itemID: String,
        lastLocation: LocationService,
        firestore: FirebaseFirestore
    ): FirebaseResponse {
        val response = FirebaseResponse()
        try {
            val userRef = firestore.collection(USERS_DB).document(uid)
            if (!userRef.get().await().exists()) {
                response.onError = Exception("User doesn't exist")
                return response
            }

            val itemRef = userRef.collection(ITEMS_DB).document(itemID)
            if (!itemRef.get().await().exists()) {
                response.onError = Exception("Item doesn't exist")
                return response
            }

            itemRef.update(mapOf(
                "last_location" to lastLocation.toFirebaseGeoPoint()
            )).await()

            response.onSuccess = true

        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /**
     * Updates the fields of a given item in Firebase. To call after its fields were updated in local.
     *
     * @param item the updated item with the fields to update in Firebase
     * @param uid the owner's UID of the item
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun updateItemFields(
        item: Item,
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

            val itemRef = userRef.collection(ITEMS_DB).document(item.getItemID())
            if (!itemRef.get().await().exists()) {
                response.onError = Exception("Item doesn't exist")
                return response
            }

            val map = mutableMapOf<String, Any>(
                "item_name" to item.itemName,
                "item_type" to item.getItemTypeID(),
                "item_description" to item.itemDesc,
                "is_lost" to item.isLost(),
            )
            if(item.getLastLocation() != null){
                map["last_location"] = item.getLastLocation()!!.toFirebaseGeoPoint()
            }

            itemRef.update(map).await()

            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }
        return response
    }

    /**
     * Deletes an item from Firebase, given its item ID.
     *
     * @param itemID the ID of the item to delete
     * @param uid the owner's UID of the item
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun deleteItemFromUser(
        itemID: String,
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

            val itemRef = userRef.collection(ITEMS_DB).document(itemID)
            if (!itemRef.get().await().exists()) {
                response.onError = Exception("Item doesn't exist")
                return response
            }

            itemRef.delete().await()

            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /**
     * Adds a new item to the database with a specific ID.
     *
     * @param item the item to add
     * @param itemID the specific ID that references the new item
     * @param uid the user ID of the item's owner
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun addItemWithItemID(
        item: Item,
        itemID: String,
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

            val map = mutableMapOf<String, Any>(
                "item_name" to item.itemName,
                "item_type" to item.getItemTypeID(),
                "item_description" to item.itemDesc,
                "is_lost" to item.isLost(),
            )
            if(item.getLastLocation() != null){
                map["last_location"] = item.getLastLocation()!!.toFirebaseGeoPoint()
            }

            userRef.collection(ITEMS_DB).document(itemID).set(map).await()

            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /**
     * Retrieves a specific item from a user.
     *
     * @param uid the user ID of the item's owner
     * @param itemID the item ID belonging to the requested item
     *
     * @return the item if the operation was successful, null otherwise.
     */
    suspend fun getSingleItemFromIDs(uid: String, itemID: String): Item? {
        return try {
            val userRef = Firebase.firestore.collection(USERS_DB).document(uid)
            if(!userRef.get().await().exists()){
                Log.e("FIREBASE ERROR","User doesn't exist")
                return null
            }

            val itemDoc = userRef.collection(ITEMS_DB).document(itemID).get().await()
            if(!itemDoc.exists()){
                Log.e("FIREBASE ERROR","Item doesn't exist")
                return null
            }

            getItemFromDoc(itemDoc)

        } catch(e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            null
        }
    }

    /**
     * Retrieves the list of items belonging to a user in real-time and saves it to the cache,
     * given a user ID.
     *
     * @param uid the user ID
     *
     */
    fun getRealtimeUserItemsFromUID(
        uid: String,
        observer: LifecycleOwner,
        firestore: FirebaseFirestore
    ) {
        val userRef = firestore.collection(USERS_DB).document(uid)
        userRef.get().addOnCompleteListener { task ->
            if(task.isSuccessful){
                userRef.collection(ITEMS_DB).addSnapshotListener { value, error ->
                    if(value != null && error == null){
                        liveData(Dispatchers.IO){
                            emit(
                                value.mapNotNull { itemDoc ->
                                    getItemFromDoc(itemDoc)
                                }
                            )
                        }.observe(observer) { list ->
                            BrugDataCache.setItemsInCache(list.toMutableList())
                        }
                    } else {
                        Log.e("FIREBASE ERROR", error?.message.toString())
                    }
                }
            } else {
                Log.e("FIREBASE ERROR", task.exception?.message.toString())
            }
        }
    }

    /* RESERVED FOR TESTS */
    suspend fun deleteAllUserItems(uid: String, firestore: FirebaseFirestore): FirebaseResponse {
        val response = FirebaseResponse()

        try {
            val userRef = firestore.collection(USERS_DB).document(uid)
            if (!userRef.get().await().exists()) {
                response.onError = Exception("User doesn't exist")
                return response
            }

            userRef.collection(ITEMS_DB).get().await().mapNotNull { item ->
                deleteItemFromUser(
                    item.id,
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

    private fun getItemFromDoc(itemDoc: DocumentSnapshot): Item? {
        try {
            if (!itemDoc.contains("item_name")
                || !itemDoc.contains("item_type")
                || !itemDoc.contains("item_description")
                || !itemDoc.contains("is_lost")
            ) {
                Log.e("FIREBASE ERROR", "Invalid Item Format")
                return null
            }

            val item = Item(
                itemDoc["item_name"] as String,
                (itemDoc["item_type"] as Long).toInt(),
                itemDoc["item_description"] as String,
                itemDoc["is_lost"] as Boolean
            )
            item.setItemID(itemDoc.id)
            if(itemDoc.contains("last_location")){
                val location = itemDoc["last_location"] as GeoPoint
                item.setLastLocation(location.longitude, location.latitude)
            }
            return item
        } catch (e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            return null
        }
    }
}

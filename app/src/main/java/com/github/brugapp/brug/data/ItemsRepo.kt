package com.github.brugapp.brug.data

import android.util.Log
import com.github.brugapp.brug.model.MyItem
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

private const val USERS_DB = "Users"
private const val ITEMS_DB = "Items"

/**
 * Repository class handling bindings between the Item objects in Firebase & in local.
 */
object ItemsRepo {
    /**
     * Adds a new item to a Firebase user, given a user ID.
     *
     * @param item the item to add
     * @param uid the user ID
     *
     * @return FirebaseResponse object denoting if the action was successful
     */
    suspend fun addItemToUser(item: MyItem, uid: String): FirebaseResponse {
        val response = FirebaseResponse()

        try {
            val userRef = Firebase.firestore.collection(USERS_DB).document(uid)
            if(!userRef.get().await().exists()){
                response.onError = Exception("User doesn't exist")
                return response
            }

            userRef.collection(ITEMS_DB).document(item.itemID).set(mapOf(
                    "item_name" to item.getItemName(),
                    "item_type" to item.getItemTypeID(),
                    "item_description" to item.getItemDesc(),
                    "is_lost" to item.isLost()
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
    suspend fun updateItemFields(item: MyItem, uid: String): FirebaseResponse {
        val response = FirebaseResponse()
        try {
            val userRef = Firebase.firestore.collection(USERS_DB).document(uid)
            if(!userRef.get().await().exists()){
                response.onError = Exception("User doesn't exist")
                return response
            }

            val itemRef = userRef.collection(ITEMS_DB).document(item.itemID)
            if(!itemRef.get().await().exists()){
                response.onError = Exception("Item doesn't exist")
                return response
            }

            itemRef.update(mapOf(
                "item_name" to item.getItemName(),
                "item_type" to item.getItemTypeID(),
                "item_description" to item.getItemDesc(),
                "is_lost" to item.isLost()
            )).await()
            response.onSuccess = true
        } catch(e: Exception) {
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
    suspend fun deleteItemFromUser(itemID: String, uid: String): FirebaseResponse {
        val response = FirebaseResponse()

        //TODO: MAKE SURE THE ITEM ID IS RETRIEVED WITH ALL THE OTHER INFOS !
        try {
            val userRef = Firebase.firestore.collection(USERS_DB).document(uid)
            if(!userRef.get().await().exists()){
                response.onError = Exception("User doesn't exist")
                return response
            }

            val itemRef = userRef.collection(ITEMS_DB).document(itemID)
            if(!itemRef.get().await().exists()){
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
     * Retrieves the list of items belonging to a user, given its user ID.
     *
     * @param uid the user ID
     *
     * @return List<MyItem> containing all the user's items, or a null value in case of error.
     */
    suspend fun getUserItemsFromUID(uid: String): List<MyItem>? {
        return try {
            val userRef = Firebase.firestore.collection(USERS_DB).document(uid)
            if(!userRef.get().await().exists()){
                Log.e("FIREBASE ERROR","User doesn't exist")
                return null
            }

            userRef.collection(ITEMS_DB).get().await().mapNotNull { item ->
                getItemFromDoc(item)
            }

        } catch(e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            null
        }
    }


    private fun getItemFromDoc(itemDoc: QueryDocumentSnapshot): MyItem?{
        try {
            if(!itemDoc.contains("item_name")
                || !itemDoc.contains("item_type")
                || !itemDoc.contains("item_description")
                || !itemDoc.contains("is_lost")){
                Log.e("FIREBASE ERROR", "Invalid Item Format")
                return null
            }

            return MyItem(
                itemDoc.id,
                itemDoc["item_name"] as String,
                (itemDoc["item_type"] as Long).toInt(),
                itemDoc["item_description"] as String,
                itemDoc["is_lost"] as Boolean
            )
        } catch (e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            return null
        }
    }
}
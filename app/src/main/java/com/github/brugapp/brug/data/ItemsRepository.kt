package com.github.brugapp.brug.data

import android.util.Log
import com.github.brugapp.brug.model.MyItem
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
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
        item: MyItem,
        uid: String,
        firestore: FirebaseFirestore,
        isCached: Boolean = false
    ): FirebaseResponse {
        Log.d("ItemsRepository", "addItemToUser: $item, $uid")
        val response = FirebaseResponse()

        try {
            val userRef = firestore.collection(USERS_DB).document(uid)
            if (!userRef.get().await().exists()) {
                response.onError = Exception("User doesn't exist")
                return response
            }

            if(isCached) {
                //control if items is already in the databaase
                //TODO: control if the item in the database is the same
                val itemRef = userRef.collection(ITEMS_DB).document(item.getItemID())
                if (itemRef.get().await().exists()) {
                    itemRef.update("is_deleted", false).await()
                    response.onSuccess = true
                }else{
                    response.onSuccess = false
                }

            }
            else{
                Log.d("ItemRepository","Is not cached")
                val id = userRef.collection(ITEMS_DB).add(
                    mapOf(
                        "item_name" to item.itemName,
                        "item_type" to item.itemTypeID,
                        "item_description" to item.itemDesc,
                        "is_lost" to item.isLost(),
                        "is_deleted" to item.getIsDeleted(),
                        "deleted_date" to item.getDeletedWhen()
                    )
                ).await().id
                Log.d("ITEM_ID",id)
                item.setItemID(id)

                response.onSuccess = true
            }
        } catch (e: Exception) {
            response.onError = e
            Log.d("ItemRepository","We have a problem amirite ${e}")
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
        item: MyItem,
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

            itemRef.update(
                mapOf(
                    "item_name" to item.itemName,
                    "item_type" to item.itemTypeID,
                    "item_description" to item.itemDesc,
                    "is_lost" to item.isLost(),
                    "is_deleted" to item.getIsDeleted(),
                    "deleted_date" to item.getDeletedWhen()
                )
            ).await()
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
        firestore: FirebaseFirestore,
        isCached: Boolean = false
    ): FirebaseResponse {
        val response = FirebaseResponse()

        //TODO: MAKE SURE THE ITEM ID IS RETRIEVED WITH ALL THE OTHER INFOS !
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
            if(!isCached) {
                itemRef.delete().await()
            }else{
                //Do not forget to modify the item's is_deleted field
                // here we only modify the is_deleted field in the database
                //itemRef.update("item_description", "Pourquoi ca ne marche pas").await()

            }

            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    /* RESERVED FOR TESTS */
    suspend fun addItemWithItemID(
        item: MyItem,
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

            userRef.collection(ITEMS_DB).document(itemID).set(
                mapOf(
                    "item_name" to item.itemName,
                    "item_type" to item.itemTypeID,
                    "item_description" to item.itemDesc,
                    "is_lost" to item.isLost(),
                    "is_deleted" to item.getIsDeleted(),
                    "deleted_date" to item.getDeletedWhen()
                )
            ).await()

            response.onSuccess = true
        } catch (e: Exception) {
            response.onError = e
        }

        return response
    }

    suspend fun deleteAllUserItems(uid: String, firestore: FirebaseFirestore): FirebaseResponse {
        val response = FirebaseResponse()

        try {
            val userRef = firestore.collection(USERS_DB).document(uid)
            if (!userRef.get().await().exists()) {
                response.onError = Exception("User doesn't exist")
                return response
            }

            userRef.collection(ITEMS_DB).get().await().mapNotNull { item ->
                deleteItemFromUser(item.id, uid, firestore)
            }

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
    suspend fun getUserItemsFromUID(uid: String, firestore: FirebaseFirestore): List<MyItem>? {
        return getListOption(firestore, uid) { !it.getIsDeleted() }
    }

    suspend fun getUserDeletedItemsFromUid(uid:String, firestore: FirebaseFirestore): List<MyItem> ? {
        return getListOption(firestore,uid) {it.getIsDeleted()}
    }


    private suspend fun getListOption(
        firestore: FirebaseFirestore,
        uid: String,
        option: (MyItem) -> Boolean
    ): List<MyItem>? {
        return try {
            val userRef = firestore.collection(USERS_DB).document(uid)
            if (!userRef.get().await().exists()) {
                Log.e("FIREBASE ERROR", "User doesn't exist")
                return null
            }

            deleteOldDeletedItems(userRef)

            userRef.collection(ITEMS_DB).get().await().mapNotNull { item ->
                getItemFromDoc(item)
            }.filter(option)

        } catch (e: Exception) {
            Log.e("FIREBASE ERROR", e.message.toString())
            null
        }
    }


    private suspend fun deleteOldDeletedItems(userRef: DocumentReference) {
        userRef.collection(ITEMS_DB).get().await().mapNotNull { item ->
            val myItem = getItemFromDoc(item)
            if (myItem != null) {
                if (myItem.isTooOld()) {
                    userRef.collection(ITEMS_DB).document(item.id).delete().await()
                }
            }
        }
    }


    private fun getItemFromDoc(itemDoc: QueryDocumentSnapshot): MyItem? {
        try {
            if (!itemDoc.contains("item_name")
                || !itemDoc.contains("item_type")
                || !itemDoc.contains("item_description")
                || !itemDoc.contains("is_lost")
                || !itemDoc.contains("is_deleted")
                || !itemDoc.contains("deleted_date")
            ) {
                Log.e("FIREBASE ERROR", "Invalid Item Format")
                Log.e("ITEM IS","${itemDoc.data}" )
                return null
            }

            val item = MyItem(
                itemDoc["item_name"] as String,
                (itemDoc["item_type"] as Long).toInt(),
                itemDoc["item_description"] as String,
                itemDoc["is_lost"] as Boolean,
            )
            item.setItemID(itemDoc.id)
            item.setDeleted(itemDoc["is_deleted"] as Boolean)
            item.setDeletedWhen(itemDoc["deleted_date"] as String)
            return item
        } catch (e: Exception) {
            Log.e("FIREBASE ERROR TRANSFORMATION", e.message.toString())
            return null
        }
    }
}
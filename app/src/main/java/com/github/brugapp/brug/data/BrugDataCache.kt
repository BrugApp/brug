package com.github.brugapp.brug.data

import androidx.lifecycle.MutableLiveData
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.User

const val NETWORK_ERROR_MSG = "ERROR: You have no network connectivity, loading elements from cache."
const val ACTION_LOST_ERROR_MSG = "ERROR: You have no network connectivity. Try again later."

object BrugDataCache {
    private val cachedUser = MutableLiveData<User>()
    private val cachedItemsList = MutableLiveData<MutableList<Item>>()
    private val cachedConvList = MutableLiveData<MutableList<Conversation>>()
    private val cachedMessagesLists = HashMap<String, MutableLiveData<MutableList<Message>>>()

    // USER PART
    /**
     * Sets a user in cache.
     * @param user the user to set in the cache
     */
    fun setUserInCache(user: User){
        this.cachedUser.postValue(user)
    }

    /**
     * Returns an observable of the cached user.
     *
     * @return the observable
     */
    fun getCachedUser(): MutableLiveData<User> {
        return this.cachedUser
    }

    /**
     * Resets the observable holding the cached user.
     */
    fun resetCachedUser() {
        this.cachedUser.postValue(null)
    }


    // ITEMS PART
    /**
     * Sets a list of items in cache.
     * @param items the list of items to set in the cache
     */
    fun setItemsInCache(items: MutableList<Item>){
        this.cachedItemsList.postValue(items)
    }

    /**
     * Returns an observable of the cached list of items.
     *
     * @return the observable
     */
    fun getCachedItems(): MutableLiveData<MutableList<Item>> {
        return this.cachedItemsList
    }

    /**
     * Resets the observable holding the cached list of items.
     */
    fun resetCachedItems() {
        this.cachedItemsList.postValue(mutableListOf())
    }

    // CONVERSATIONS PART
    /**
     * Sets a list of conversations in cache.
     * @param conversations the list of conversations to set in the cache
     */
    fun setConversationsInCache(conversations: MutableList<Conversation>){
        this.cachedConvList.postValue(conversations)
    }

    /**
     * Returns an observable of the cached list of conversations.
     *
     * @return the observable
     */
    fun getCachedConversations(): MutableLiveData<MutableList<Conversation>> {
        return this.cachedConvList
    }

    /**
     * Resets the observable holding the cached list of conversations.
     */
    fun resetCachedConversations() {
        this.cachedConvList.postValue(mutableListOf())
    }

    // MESSAGES LIST PART
    /**
     * Initializes a new entry for a given conversation ID
     * to hold its associated list of messages in cache.
     *
     * @param convID the conversation ID
     */
    fun initMessageListInCache(convID: String) {
        if(!cachedMessagesLists.containsKey(convID)){
            this.cachedMessagesLists[convID] = MutableLiveData<MutableList<Message>>()
        }
    }

    /**
     * Sets a list of messages in cache.
     *
     * @param convID the ID of the associated conversation
     * @param messages the list of messages to set in the cache
     */
    fun setMessageListInCache(convID: String, messages: MutableList<Message>){
        this.cachedMessagesLists[convID]!!.postValue(messages)
    }

    /**
     * Deletes the list of messages from the cache, for a given conversation ID.
     *
     * @param convID the conversation ID
     */
    fun deleteCachedMessageList(convID: String) {
        this.cachedMessagesLists[convID] = MutableLiveData<MutableList<Message>>()
    }

    /**
     * Returns an observable of the cached list of messages belonging to a given conversation.
     *
     * @param convID the conversation ID associated to the requested list of messages
     *
     * @return the observable
     */
    fun getCachedMessageList(convID: String): MutableLiveData<MutableList<Message>> {
        return this.cachedMessagesLists[convID]!!
    }

    /**
     * Resets all cached messages lists.
     */
    fun resetCachedMessagesLists() {
        this.cachedMessagesLists.clear()
    }

    /**
     * Checks whether there is network connectivity.
     *
     * @return true if there is network connectivity, false otherwise.
     */
    fun isNetworkAvailable(): Boolean {
        return try {
            val command = "ping -c 1 google.com"
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

}
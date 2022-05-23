package com.github.brugapp.brug.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.User
import java.util.concurrent.TimeUnit

const val NETWORK_ERROR_MSG = "ERROR: You have no network connectivity, loading elements from cache."
const val ACTION_LOST_ERROR_MSG = "ERROR: You have no network connectivity. Try again later."

object BrugDataCache {
    private val cachedUser = MutableLiveData<User>()
    private val cachedItemsList = MutableLiveData<MutableList<Item>>()
    private val cachedConvList = MutableLiveData<MutableList<Conversation>>()
    private val cachedMessagesLists = HashMap<String, MutableLiveData<MutableList<Message>>>()

    // USER PART
    fun setUserInCache(user: User){
        this.cachedUser.postValue(user)
    }

    fun getCachedUser(): MutableLiveData<User> {
        return this.cachedUser
    }

    fun resetCachedUser() {
        this.cachedUser.postValue(null)
    }


    // ITEMS PART
    fun setItemsInCache(items: MutableList<Item>){
        this.cachedItemsList.postValue(items)
    }

    fun getCachedItems(): MutableLiveData<MutableList<Item>> {
        return this.cachedItemsList
    }

    fun resetCachedItems() {
        this.cachedItemsList.postValue(mutableListOf())
    }

    // CONVERSATIONS PART
    fun setConversationsInCache(conversations: MutableList<Conversation>){
        this.cachedConvList.postValue(conversations)
    }

    fun getCachedConversations(): MutableLiveData<MutableList<Conversation>> {
        return this.cachedConvList
    }

    fun resetCachedConversations() {
        this.cachedConvList.postValue(mutableListOf())
    }

    // MESSAGES LIST PART
    fun initMessageListInCache(convID: String) {
        if(!cachedMessagesLists.containsKey(convID)){
            this.cachedMessagesLists[convID] = MutableLiveData<MutableList<Message>>()
        }
    }

    fun setMessageListInCache(convID: String, messages: MutableList<Message>){
        this.cachedMessagesLists[convID]!!.postValue(messages)
    }

    fun deleteCachedMessageList(convID: String) {
        this.cachedMessagesLists[convID] = MutableLiveData<MutableList<Message>>()
    }

    fun getCachedMessageList(convID: String): MutableLiveData<MutableList<Message>> {
        return this.cachedMessagesLists[convID]!!
    }


    fun resetCachedMessagesLists() {
        this.cachedMessagesLists.clear()
    }

    fun isNetworkAvailable(): Boolean {
        return try {
            val command = "ping -c 1 google.com"
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

}
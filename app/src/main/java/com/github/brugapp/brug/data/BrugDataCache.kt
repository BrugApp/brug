package com.github.brugapp.brug.data

import androidx.lifecycle.MutableLiveData
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.User

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

    fun getCachedMessageList(convID: String): MutableLiveData<MutableList<Message>> {
        return this.cachedMessagesLists[convID]!!
    }

    fun deleteCachedMessageList(convID: String) {
        this.cachedMessagesLists.remove(convID)
    }

    fun resetCachedMessagesLists() {
        this.cachedMessagesLists.clear()
    }



}
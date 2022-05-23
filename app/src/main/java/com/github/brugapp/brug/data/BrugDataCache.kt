package com.github.brugapp.brug.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Message

object BrugDataCache {
    private val conversationsList: MutableLiveData<MutableList<Conversation>> = MutableLiveData()
    private val messageMap: HashMap<String, MutableLiveData<MutableList<Message>>> = HashMap()

    fun setConversationsList(data: List<Conversation>?) {
        conversationsList.value = data?.toMutableList() ?: mutableListOf()
    }

    fun getConversationList(): MutableLiveData<MutableList<Conversation>> {
        return this.conversationsList
    }

    fun addMessageList(convID: String, data: MutableList<Message>){
        if(!messageMap.containsKey(convID)){
            messageMap[convID] = MutableLiveData()
        }
        messageMap[convID]!!.value = data
    }

    fun getMessageList(convID: String): MutableLiveData<MutableList<Message>> {
        if(!messageMap.containsKey(convID)){
            addMessageList(convID, mutableListOf())
        }
        Log.e("LIVEDATA STATE", "GOT ENTRY !")
        return this.messageMap[convID]!!
    }

    fun resetConversationsList() {
        this.conversationsList.postValue(mutableListOf())
    }

    fun resetMessages() {
        this.messageMap.clear()
    }
}
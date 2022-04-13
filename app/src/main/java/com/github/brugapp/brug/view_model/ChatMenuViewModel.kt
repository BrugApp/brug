package com.github.brugapp.brug.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.model.Conversation
import kotlinx.coroutines.Dispatchers


/**
 * ViewModel of the Chat Menu UI, handling its UI logic.
 */
class ChatMenuViewModel : ViewModel() {
    //TODO: CHANGE WITH PROPER CALL WHEN BINDINGS WITH FIREBASE ARE FINALIZED
    private val conversationsLiveData = liveData(Dispatchers.IO) {
        emit(FirebaseHelper.getConversationsFromUserID("7IsGzvjHKd0KeeKK722m"))
    }

    /**
     * Getter for the list of conversations.
     */
    fun getConversationsLiveData(): LiveData<MutableList<Conversation>?> {
        return conversationsLiveData
    }
}
package com.github.brugapp.brug.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.data.TempConvListResponse
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
    fun getConversationsLiveData(): LiveData<TempConvListResponse> {
        return conversationsLiveData
    }

    //DON'T KNOW WHAT IS THIS FOR
//        println("===========================================================")
//        println(Uri.parse("content://com.github.brugapp.brug.fileprovider/images/JPEG_20220329_165630_1949197185066641075.jpg"))
//        println("===========================================================")
}
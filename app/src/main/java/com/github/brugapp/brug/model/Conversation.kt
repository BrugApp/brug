package com.github.brugapp.brug.model

import com.github.brugapp.brug.data.UserFieldsResponse
import com.github.brugapp.brug.data.MessageResponse
import com.github.brugapp.brug.data.ItemNameResponse
import java.io.Serializable

data class Conversation(
    val convId: String,
    val userFieldsInfos: UserFieldsResponse,
    val lostItemName: ItemNameResponse,
    val messages: MutableList<MessageResponse>
): Serializable

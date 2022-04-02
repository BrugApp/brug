package com.github.brugapp.brug.model

import com.github.brugapp.brug.data.ConvUserResponse
import com.github.brugapp.brug.data.MessageResponse
import com.github.brugapp.brug.data.ItemNameResponse
import java.io.Serializable

data class Conversation(
    val userInfos: ConvUserResponse,
    val lostItemName: ItemNameResponse,
    val messages: MutableList<MessageResponse>
): Serializable

package com.github.brugapp.brug.data

import com.github.brugapp.brug.model.Message
import java.io.Serializable
import java.lang.Exception

data class MessageResponse(var onSuccess: Message? = null, var onError: Exception? = null): Serializable

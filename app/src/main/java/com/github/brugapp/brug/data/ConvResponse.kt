package com.github.brugapp.brug.data

import com.github.brugapp.brug.model.Conversation
import java.io.Serializable
import java.lang.Exception

data class ConvResponse(var onSuccess: Conversation? = null, var onError: Exception? = null): Serializable

package com.github.brugapp.brug.data

import java.io.Serializable
import java.lang.Exception

data class StringResponse(var onSuccess: String? = null, var onError: Exception? = null): Serializable

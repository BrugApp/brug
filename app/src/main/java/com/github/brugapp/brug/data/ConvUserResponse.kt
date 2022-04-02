package com.github.brugapp.brug.data

import java.io.Serializable

/**
 * Holds either a pair (fullname, iconPath), or an exception.
 */
data class ConvUserResponse(var onSuccess: Pair<String, String>? = null, var onError: Exception? = null): Serializable

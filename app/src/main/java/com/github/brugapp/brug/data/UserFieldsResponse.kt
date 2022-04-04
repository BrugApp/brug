package com.github.brugapp.brug.data

import java.io.Serializable

/**
 * Holds either a pair (fullname, iconPath), or an exception.
 */
data class UserFieldsResponse(var onSuccess: Pair<String, FileResponse>? = null, var onError: Exception? = null): Serializable

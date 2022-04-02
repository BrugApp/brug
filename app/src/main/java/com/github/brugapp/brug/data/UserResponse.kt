package com.github.brugapp.brug.data

import com.github.brugapp.brug.model.User
import java.lang.Exception

data class UserResponse(var onSuccess: User? = null, var onError: Exception? = null)

package com.github.brugapp.brug.data

import java.io.File
import java.io.Serializable

data class FileResponse(var onSuccess: File? = null, var onError: Exception? = null): Serializable

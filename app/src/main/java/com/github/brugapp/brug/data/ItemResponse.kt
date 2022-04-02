package com.github.brugapp.brug.data

import com.github.brugapp.brug.model.Item
import java.lang.Exception

data class ItemResponse(var onSuccess: Item? = null, var onError: Exception? = null)

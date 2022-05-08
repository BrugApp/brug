package com.github.brugapp.brug.model

import android.graphics.drawable.Drawable
import java.io.Serializable

data class MyUser(
    val uid: String,
    val firstName: String,
    val lastName: String,
    private var userIconPath: String?
): Serializable {

    fun getFullName(): String {
        return "$firstName $lastName"
    }

    fun getUserIconPath(): String? {
        return this.userIconPath
    }
}
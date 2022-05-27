package com.github.brugapp.brug.model

import java.io.Serializable

data class User(
    val uid: String,
    val firstName: String,
    val lastName: String,
    private var userIconPath: String?,
    val tokenList: MutableList<String>
): Serializable {

    /**
     * returns the full name of the user
     */
    fun getFullName(): String {
        return "$firstName $lastName"
    }

    /**
     * return the path of the user icon
     */
    fun getUserIconPath(): String? {
        return this.userIconPath
    }
}
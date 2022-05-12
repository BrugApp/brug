package com.github.brugapp.brug.model

import java.io.Serializable

data class MyUser(
    val uid: String,
    val firstName: String,
    val lastName: String,
    private var userIconPath: String?,
    val tokenList: MutableList<String>
): Serializable {

    fun getFullName(): String {
        return "$firstName $lastName"
    }

    fun getUserIconPath(): String? {
        return this.userIconPath
    }
}
package com.github.brugapp.brug.data

import java.io.Serializable

class DummyUser(
    val firstName: String,
    val lastName: String,
    val iconPath: String?
): Serializable {
    fun getFullName(): String {
        return "$firstName $lastName"
    }
}

package com.github.brugapp.brug.model

import android.graphics.drawable.Drawable
import java.io.Serializable

class MyUser(
    val uid: String,
    val firstName: String,
    val lastName: String,
    private var userIcon: Drawable?): Serializable {

    fun getFullName(): String {
        return "$firstName $lastName"
    }

    fun getUserIcon(): Drawable? {
        return this.userIcon
    }


    override fun equals(other: Any?): Boolean {
        val otherUser = other as MyUser
        return this.uid == otherUser.uid
                && this.firstName == otherUser.firstName
                && this.lastName == otherUser.lastName
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + (userIcon?.hashCode() ?: 0)
        return result
    }
}
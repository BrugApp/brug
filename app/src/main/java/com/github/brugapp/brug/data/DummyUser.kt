package com.github.brugapp.brug.data

class DummyUser(val firstName: String, val lastName: String, val userIcon: String?) {
    fun getFullName(): String {
        return "$firstName $lastName"
    }

}

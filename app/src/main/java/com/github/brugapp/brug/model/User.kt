package com.github.brugapp.brug.model

import java.util.regex.Pattern

class User// later generate id
    (
    private var firstName: String,
    private var lastName: String,
    private var email: String,
    private var id: String
) {
    private var items: ArrayList<Item>

    init {
        if (firstName.isBlank() || lastName.isBlank() || id.isBlank()) {
            throw IllegalArgumentException("Invalid name !")
        }
        val emailAddressPattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        if (!emailAddressPattern.matcher(email).matches()) {

            throw IllegalArgumentException("Invalid email !")

        }
        items = ArrayList()
    }

    fun getFirstName(): String {
        return firstName
    }

    fun getLastName(): String {
        return lastName
    }

    fun getId(): String {
        return id
    }

    fun getEmail(): String {
        return email
    }


}
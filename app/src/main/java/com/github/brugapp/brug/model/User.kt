package com.github.brugapp.brug.model

import java.lang.IllegalArgumentException
import java.util.regex.Pattern

class User// later generate id
    (
    private var firstName: String,
    private var lastName: String,
    private var email: String,
    private var id: Int
) {

    private var items : ArrayList<Item>

    init {
        if(firstName.isBlank() || lastName.isBlank()){
            throw IllegalArgumentException("Invalid name !")
        }
        val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        if(!EMAIL_ADDRESS_PATTERN.matcher(email).matches()){

            throw IllegalArgumentException("Invalid email !")

        }
        items = ArrayList()
    }

    fun getFirstName() : String {
        return firstName
    }

    fun getLasttName() : String {
        return lastName
    }

    fun getId() : Int {
        return id
    }

    fun getEmail() : String {
        return email
    }



}
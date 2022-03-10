package com.github.brugapp.brug.model

import java.lang.IllegalArgumentException
import java.util.regex.Pattern

class User {

    private var firstName : String
    private var lastName : String
    private var email : String
    private var id : String
    private var items : ArrayList<Item>

    // later generate id
    constructor(firstName : String, lastName : String, email : String, id : String){

        if(firstName.isBlank() || lastName.isBlank() || id.isBlank()){
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


        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.id = id

        items = ArrayList()

    }

    fun getFirstName() : String {
        return firstName
    }

    fun getLastName() : String {
        return lastName
    }

    fun getId() : String {
        return id
    }

    fun getEmail() : String {
        return email
    }



}
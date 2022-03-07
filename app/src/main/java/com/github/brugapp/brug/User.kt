package com.github.brugapp.brug

import java.util.regex.Pattern

class User {

    private var items : ArrayList<Item>
    private var id : Int
    private var firstName : String
    private var lastName : String
    private var email : String


    // For now, id is given in the constructor, generate ir later
    constructor(userId : Int, firstName : String, lastName : String, email : String){

        if(firstName.isNullOrEmpty() || firstName.trim().isEmpty()){
            throw IllegalArgumentException("First name is invalid !")
        }

        if(lastName.isNullOrEmpty() || lastName.trim().isEmpty()){
            throw IllegalArgumentException("Last name is invalid !")
        }

        // enables to verify if the given email address has a valid format
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
            throw IllegalArgumentException("Email address in invalid !")
        }

        id = userId
        this.firstName = firstName
        this.lastName = lastName
        this.email = email

        items = ArrayList<Item>()

    }

    fun getId() : Int{
        return id
    }

    fun getFirstName() : String{
        return firstName
    }

    fun getLastName() : String{
        return lastName
    }

    fun getEmail() : String{
        return email
    }


}
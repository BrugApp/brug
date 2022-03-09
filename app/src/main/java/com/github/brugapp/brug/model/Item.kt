package com.github.brugapp.brug.model

import java.lang.IllegalArgumentException

class Item {

    private var name : String
    private var id : Int
    // add QR code attribute
    // add last localization attribute

    // Generate the id later
    constructor(name : String, id : Int){

        if(name.isNullOrBlank()){
            throw IllegalArgumentException("Invalid name")
        }

        this.name = name
        this.id = id

    }

    fun getName() : String{
        return name
    }

    fun getId() : Int{
        return id
    }

    fun setName(name : String){

        if(name.isNullOrBlank()){
            throw IllegalArgumentException("Invalid new name")
        }

        this.name = name

    }


}
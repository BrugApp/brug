package com.github.brugapp.brug.model

import java.io.Serializable
import java.lang.IllegalArgumentException

class Item// Generate the id later
    (private var name: String, private var id: Int, private var description : String): Serializable {

    // add QR code attribute
    // add last localization attribute

    init {
        if(name.isBlank()){
            throw IllegalArgumentException("Invalid name")
        }
    }

    fun getName() : String{
        return name
    }

    fun getId() : Int{
        return id
    }

    fun getDescription() : String{
        return description
    }

    fun setName(name : String){

        if(name.isBlank()){
            throw IllegalArgumentException("Invalid new name")
        }

        this.name = name

    }

    fun setDescription(description : String){
        this.description = description
    }


}
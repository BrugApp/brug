package com.github.brugapp.brug.model

import java.lang.IllegalArgumentException

class Item// Generate the id later
    (private var name: String, private var image_id: Int, private var description : String, private var id : Int) {

    // add QR code attribute
    // add last localization attribute
    // image_id attribute is for future use (with image type)

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
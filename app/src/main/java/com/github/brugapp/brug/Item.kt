package com.github.brugapp.brug

class Item {

    private var name : String
    // QR code attribute
    // ID in the database
    // Last Localization

    constructor(name : String){

        if(name.isNullOrEmpty() || name.trim().isEmpty()){
            throw IllegalArgumentException("Name is invalid !")
        }

        this.name = name

        // generate ID for the database

    }

    fun getName() : String {
        return name
    }

}

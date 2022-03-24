package com.github.brugapp.brug.fake

import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.model.Item

class MockDatabase {

    companion object {
        val currentUser = User("Rayan", "Kikou", "rayan.kikou@gmail.com", "0")
        //TODO generate actual item id with the database
        var itemId = 0
    }

}
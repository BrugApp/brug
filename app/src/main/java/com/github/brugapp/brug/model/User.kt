package com.github.brugapp.brug.model

import java.io.Serializable
import java.util.regex.Pattern

class User// later generate id
    (
    private var firstName: String,
    private var lastName: String,
    private var email: String,
    private var id: String
): Serializable {
    private var items: MutableList<Item>

    init {
        if (firstName.isBlank() || lastName.isBlank() || id.isBlank()) {
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
        if (!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {

            throw IllegalArgumentException("Invalid email !")

        }
        items = mutableListOf(
            Item("Phone", "Samsung Galaxy S22", "").setType(ItemType.Phone),
            Item("Wallet", "With all my belongings", "").setType(ItemType.Wallet),
            Item("BMW Key", "BMW M3 F80 Competition", "").setType(ItemType.CarKeys),
            Item("Keys","House and everything else", "").setType(ItemType.Keys)
        )
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

    fun getItemList(): MutableList<Item> {
        return items
    }

    fun addItem(item : Item) : Boolean{

        if(item != null){
            items.add(item)
            return true
        }
        return false

    }

}
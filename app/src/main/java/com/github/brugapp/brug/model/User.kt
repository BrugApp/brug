package com.github.brugapp.brug.model

import com.github.brugapp.brug.R
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
            Item("Phone", R.drawable.ic_baseline_smartphone_24, "Samsung Galaxy S22", 0),
            Item("Wallet", R.drawable.ic_baseline_account_balance_wallet_24, "With all my belongings", 0),
            Item("BMW Key", R.drawable.ic_baseline_car_rental_24, "BMW M3 F80 Competition", 0),
            Item("Keys", R.drawable.ic_baseline_vpn_key_24,"House and everything else", 0)
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
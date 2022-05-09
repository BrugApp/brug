package com.github.brugapp.brug.model

import android.graphics.drawable.Drawable
import java.io.Serializable
import java.util.regex.Pattern


class User// later generate id
    (
    private var firstName: String,
    private var lastName: String,
    private var email: String,
    private var id: String,
    @Transient
    private var profilePicture: Drawable?
) : Serializable {
    private var items: MutableList<Item>

    private var tripleList = listOf(
        Triple("Phone", "Samsung Galaxy S22", ItemType.Phone),
        Triple("Wallet", "With all my belongings", ItemType.Wallet),
        Triple("BMW Key", "BMW M3 F80 Competition", ItemType.CarKeys),
        Triple("Keys", "House and everything else", ItemType.Keys)
    )

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
        items = tripleList.map { generateItem(it.first, it.second, it.third) }.toMutableList()
    }


    private fun generateId(): String {
        return (1..100).random().toString()
    }

    private fun generateItem(name: String, description: String, type: ItemType): Item {
        return Item(name, description, generateId()).setType(type)
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


    fun setProfilePicture(drawable: Drawable?): User {
        this.profilePicture = drawable
        return this
    }

    fun getProfilePicture(): Drawable? {
        return this.profilePicture
    }

    fun addItem(item: Item): Boolean {
        return items.add(item)
    }

}
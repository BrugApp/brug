package com.github.brugapp.brug

import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.User
import org.hamcrest.MatcherAssert.*
import org.junit.Assert
import java.lang.IllegalArgumentException
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is


class UserTest {

    @Test
    fun invalidFirstName() {
        val firstName = "   "
        Assert.assertThrows(IllegalArgumentException::class.java) {
            User(firstName, "Kikou", "rayan.kikou@gmail.com", "0")
        }
    }

    @Test
    fun emptyFirstName() {
        val firstName = ""
        Assert.assertThrows(IllegalArgumentException::class.java) {
            User(firstName, "Kikou", "rayan.kikou@gmail.com", "0")
        }
    }

    @Test
    fun invalidLastName() {
        val lastName = "   "
        Assert.assertThrows(IllegalArgumentException::class.java) {
            User("Rayan", lastName, "rayan.kikou@gmail.com", "0")
        }
    }

    @Test
    fun emptyLastName() {
        val lastName = ""
        Assert.assertThrows(IllegalArgumentException::class.java) {
            User("Rayan", lastName, "rayan.kikou@gmail.com", "0")
        }
    }

    @Test
    fun invalidEmail() {
        val email = "test.com"
        Assert.assertThrows(IllegalArgumentException::class.java) {
            User("Rayan", "Kikou", email, "0")
        }

    }

    @Test
    fun invalidID() {
        val id = ""
        Assert.assertThrows(IllegalArgumentException::class.java) {
            User("Rayan", "Kikou", "rayan.kikou@gmail.com", id)
        }

    }

    @Test
    fun validUser(){

        val firstName = "Rayan"
        val lastName = "Kikou"
        val email = "rayan.kikou@gmail.com"
        val id = "0"

        val validUser = User(firstName, lastName, email, id)

        assertThat(1, Is(1))
        assertThat(validUser.getFirstName(), Is(firstName))
        assertThat(validUser.getLastName(), Is(lastName))
        assertThat(validUser.getEmail(), Is(email))
        assertThat(validUser.getId(), Is(id))
    }

    @Test
    fun getUserItemListTest(){

        val firstName = "Rayan"
        val lastName = "Kikou"
        val email = "rayan.kikou@gmail.com"
        val id = "0"

        val validUser = User(firstName, lastName, email, id)

        val newItem = Item("Wallet", "Grey wallet", "0")
        var itemList = ArrayList<Item>(validUser.getItemList())
        validUser.addItem(newItem)
        itemList.add(newItem)

        assertThat(validUser.getItemList(), Is(itemList))
    }


}
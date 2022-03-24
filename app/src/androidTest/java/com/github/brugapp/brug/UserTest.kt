package com.github.brugapp.brug

import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.User
import org.hamcrest.MatcherAssert.*
import java.lang.IllegalArgumentException
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is


class UserTest {

    @Test(expected = IllegalArgumentException::class)
    fun invalidFirstName() {
        val firstName = "   "
        User(firstName, "Kikou", "rayan.kikou@gmail.com", "0")
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyFirstName() {
        val firstName = ""
        User(firstName, "Kikou", "rayan.kikou@gmail.com", "0")
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidLastName() {
        val lastName = "   "
        User("Rayan", lastName, "rayan.kikou@gmail.com", "0")
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyLastName() {
        val lastName = ""
        User("Rayan", lastName, "rayan.kikou@gmail.com", "0")
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidEmail() {
        val email = "test.com"
        User("Rayan", "Kikou", email, "0")
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidID() {
        val id = ""
        User("Rayan", "Kikou", "rayan.kikou@gmail.com", id)
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

        val newItem = Item("Wallet", R.drawable.ic_baseline_add_24, "Grey wallet", 0)
        validUser.addItem(newItem)

        var itemList = ArrayList<Item>()
        itemList.add(newItem)

        assertThat(validUser.getItemList(), Is(itemList))
    }


}
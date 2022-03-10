package com.github.brugapp.brug

import com.github.brugapp.brug.model.User
import org.hamcrest.MatcherAssert.*
import java.lang.IllegalArgumentException
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is


class UserTest {

    @Test(expected = IllegalArgumentException::class)
    fun invalidFirstName() {
        val firstName = "   "
        val invalidUser = User(firstName, "Kikou", "rayan.kikou@gmail.com", 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyFirstName() {
        val firstName = ""
        val invalidUser = User(firstName, "Kikou", "rayan.kikou@gmail.com", 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidLastName() {
        val lastName = "   "
        val invalidUser = User("Rayan", lastName, "rayan.kikou@gmail.com", 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyLastName() {
        val lastName = ""
        val invalidUser = User("Rayan", lastName, "rayan.kikou@gmail.com", 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidEmail() {
        val email = "test.com"
        val invalidUser = User("Rayan", "Kikou", email, 0)
    }

    @Test
    fun validUser(){

        val firstName = "Rayan"
        val lastName = "Kikou"
        val email = "rayan.kikou@gmail.com"
        val id = 0

        val validUser = User(firstName, lastName, email, id)

        assertThat(1, Is(1))
        assertThat(validUser.getFirstName(), Is(firstName))
        assertThat(validUser.getLasttName(), Is(lastName))
        assertThat(validUser.getEmail(), Is(email))
        assertThat(validUser.getId(), Is(id))
    }


}
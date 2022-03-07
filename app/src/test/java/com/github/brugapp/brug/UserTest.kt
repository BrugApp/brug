package com.github.brugapp.brug

import org.junit.Test
import org.junit.Assert.*
import java.lang.IllegalArgumentException

class UserTest {

    @Test
    fun invalidEmail() {
        var email = "test"
        assertThrows(IllegalArgumentException::class.java){
            var invalidUser = User(0, "Rayan", "Kikou", email)
        }
    }

    @Test
    fun emptyFirstName() {
        var firstName = ""
        assertThrows(IllegalArgumentException::class.java){
            var invalidUser = User(0, firstName, "Kikou", "first.last@gmail.com")
        }
    }

    @Test
    fun invalidFirstName() {
        var firstName = "    "
        assertThrows(IllegalArgumentException::class.java){
            var invalidUser = User(0, firstName, "Kikou", "first.last@gmail.com")
        }
    }

    @Test
    fun invalidLastName() {
        var lastName = " "
        assertThrows(IllegalArgumentException::class.java){
            var invalidUser = User(0, "Rayan", lastName,  "first.last@gmail.com")
        }
    }

    @Test
    fun emptyLastName() {
        var lastName = ""
        assertThrows(IllegalArgumentException::class.java){
            var invalidUser = User(0, "Rayan", lastName,  "first.last@gmail.com")
        }
    }

    @Test
    fun validUser() {

        var firstName = "Rayan"
        var lastName = "Kikou"
        var email = "first.last@gmail.com"
        var id = 0
        var validUser = User(id, firstName, lastName,email)

        assert(validUser.getFirstName().equals(firstName))
        assert(validUser.getLastName().equals(lastName))
        assert(validUser.getEmail().equals(email))
        assert(validUser.getId().equals(id))

    }




}
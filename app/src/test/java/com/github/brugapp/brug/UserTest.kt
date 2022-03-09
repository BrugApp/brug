package com.github.brugapp.brug

import org.junit.Assert.*
import java.lang.IllegalArgumentException
import org.junit.Test

class UserTest {

    @Test
    fun invalidFirstName(){

        val firstName = "   "

        assertThrows(IllegalArgumentException::class.java){
            val invalidUser = User(firstName, "Kikou", "rayan.kikou@gmail.com", 0)
        }

    }

    @Test
    fun emptyFirstName(){

        val firstName = ""

        assertThrows(IllegalArgumentException::class.java){
            val invalidUser = User(firstName, "Kikou", "rayan.kikou@gmail.com", 0)
        }

    }

    @Test
    fun invalidLastName(){

        val lastName = "   "

        assertThrows(IllegalArgumentException::class.java){
            val invalidUser = User("Rayan", lastName, "rayan.kikou@gmail.com", 0)
        }

    }

    @Test
    fun emptyLastName(){

        val lastName = ""

        assertThrows(IllegalArgumentException::class.java){
            val invalidUser = User("Rayan", lastName, "rayan.kikou@gmail.com", 0)
        }

    }

    @Test
    fun invalidEmail(){

        val email = "test.com"

        assertThrows(IllegalArgumentException::class.java){
            val invalidUser = User("Rayan", "Kikou", email, 0)
        }

    }

    @Test
    fun validUser(){

        val firstName = "Rayan"
        val lastName = "Kikou"
        val email = "rayan.kikou@gmail.com"
        val id = 0


        val validUser = User(firstName, lastName, email, id)

        assert(validUser.getFirstName().equals(firstName))
        assert(validUser.getLasttName().equals(lastName))
        assert(validUser.getEmail().equals(email))
        assert(validUser.getId() == id)

    }





}
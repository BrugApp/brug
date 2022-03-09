package com.github.brugapp.brug

import org.junit.Assert.*
import java.lang.IllegalArgumentException
import org.junit.Test

class ItemTest {

    @Test
    fun invalidNameItem(){
        val blankName = "    "
        assertThrows(IllegalArgumentException::class.java){
            val invalidUser = Item(blankName, 1)
        }
    }

    @Test
    fun emptyNameItem(){
        val emptyName = ""
        assertThrows(IllegalArgumentException::class.java){
            val invalidUser = Item(emptyName, 1)
        }
    }

    @Test
    fun validNameItem(){
        val itemName = "Wallet"
        val itemId = 1
        val validUser = Item(itemName, itemId)

        assert(validUser.getName().equals(itemName))


    }

    @Test
    fun validIdItem(){
        val itemName = "Wallet"
        val itemId = 1
        val validUser = Item(itemName, itemId)

        assert(validUser.getId() == itemId)

    }

    @Test
    fun setInvalidName(){
        val itemName = "Wallet"
        val itemId = 1
        val validUser = Item(itemName, itemId)

        val invalidName = "    "

        assertThrows(IllegalArgumentException::class.java){
            validUser.setName(invalidName)
        }
    }

    @Test
    fun setValidName(){
        val itemName = "Wallet"
        val itemId = 1
        val validUser = Item(itemName, itemId)

        val validName = "Bag"


        validUser.setName(validName)

        assert(validUser.getName().equals(validName))
    }




}







package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.lang.IllegalArgumentException

@RunWith(AndroidJUnit4::class)
class ItemTest {

    @Test
    fun invalidNameItem(){
        var blankName = "    "
        assertThrows(IllegalArgumentException::class.java){
            var invalidUser = Item(blankName, 1)
        }
    }

    @Test
    fun emptyNameItem(){
        var emptyName = ""
        assertThrows(IllegalArgumentException::class.java){
            var invalidUser = Item(emptyName, 1)
        }
    }

    @Test
    fun validNameItem(){
        var itemName = "Wallet"
        var itemId = 1
        var validUser = Item(itemName, itemId)

        assert(validUser.getName().equals(itemName))


    }

    @Test
    fun validIdItem(){
        var itemName = "Wallet"
        var itemId = 1
        var validUser = Item(itemName, itemId)

        assert(validUser.getId() == itemId)

    }

    @Test
    fun setInvalidName(){
        var itemName = "Wallet"
        var itemId = 1
        var validUser = Item(itemName, itemId)

        var invalidName = "    "

        assertThrows(IllegalArgumentException::class.java){
            validUser.setName(invalidName)
        }
    }

    @Test
    fun setValidName(){
        var itemName = "Wallet"
        var itemId = 1
        var validUser = Item(itemName, itemId)

        var validName = "Bag"


        validUser.setName(validName)

        assert(validUser.getName().equals(validName))
    }




}







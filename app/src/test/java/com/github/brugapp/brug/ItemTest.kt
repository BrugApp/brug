package com.github.brugapp.brug

import org.junit.Test
import org.junit.Assert.*
import java.lang.IllegalArgumentException

class ItemTest {

    @Test
    fun emptyItemName(){
        var itemName = ""
        assertThrows(IllegalArgumentException::class.java){
            var invalidItem = Item(itemName)
        }
    }

    @Test
    fun invalidItemName(){
        var itemName = "     "
        assertThrows(IllegalArgumentException::class.java){
            var invalidItem = Item(itemName)
        }
    }

    @Test
    fun validItem(){
        var itemName = "Wallet"
        var validItem = Item(itemName)

        assert(validItem.getName().equals(itemName))
    }

}
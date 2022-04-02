package com.github.brugapp.brug

import com.github.brugapp.brug.model.Item
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

class ItemTest {

    @Test
    fun invalidNameItemTest() {
        val blankName = "    "
        val description = "Grey wallet"
        assertThrows(IllegalArgumentException::class.java) {
            Item(blankName, description, "1")
        }
    }

    @Test
    fun emptyNameItemTest() {
        val emptyName = ""
        val description = "Grey wallet"
        assertThrows(IllegalArgumentException::class.java) {
            Item(emptyName, description, "1")
        }
    }

    @Test
    fun validNameItemTest(){
        val itemName = "Wallet"
        val itemId = "1"
        val description = "Grey wallet"
        val validUser = Item(itemName, description, itemId)

        assertThat(validUser.getName(), Is(itemName))

    }

    @Test
    fun validIdItemTest(){
        val itemName = "Wallet"
        val itemId = "1"
        val description = "Grey wallet"
        val validUser = Item(itemName, description, itemId)

        assertThat(validUser.getId(), Is(itemId))

    }

    @Test
    fun descriptionTest(){
        val itemName = "Wallet"
        val itemId = "1"
        val description = "Grey wallet"
        val validUser = Item(itemName, description, itemId)

        assertThat(validUser.getDescription(), Is(description))

    }

    @Test
    fun setInvalidNameTest() {
        val itemName = "Wallet"
        val itemId = "1"
        val description = "Grey wallet"
        val validUser = Item(itemName, description, itemId)

        val invalidName = "    "

        assertThrows(IllegalArgumentException::class.java) {
            validUser.setName(invalidName)
        }
    }

    @Test
    fun setValidNameTest(){
        val itemName = "Wallet"
        val itemId = "1"
        val description = "Grey wallet"
        val validUser = Item(itemName, description, itemId)

        val validName = "Bag"

        validUser.setName(validName)

        assertThat(validUser.getName(), Is(validName))
    }

    @Test
    fun setDescriptionTest(){
        val itemName = "Wallet"
        val itemId = "1"
        val description = "Grey wallet"
        val validUser = Item(itemName, description, itemId)

        val newDescription = "Black wallet"

        validUser.setDescription(newDescription)

        assertThat(validUser.getDescription(), Is(newDescription))
    }

}







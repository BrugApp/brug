package com.github.brugapp.brug

import com.github.brugapp.brug.model.Item
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

class ItemTest {

    @Test(expected = IllegalArgumentException::class)
    fun invalidNameItemTest() {
        val blankName = "    "
        val description = "Grey wallet"
        Item(blankName, 1, description)

    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyNameItemTest() {
        val emptyName = ""
        val description = "Grey wallet"
        Item(emptyName, 1, description)
    }

    @Test
    fun validNameItemTest(){
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, itemId, description)

        assertThat(validUser.getName(), Is(itemName))

    }

    @Test
    fun validIdItemTest(){
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, itemId, description)

        assertThat(validUser.getId(), Is(itemId))

    }

    @Test
    fun descriptionTest(){
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, itemId, description)

        assertThat(validUser.getDescription(), Is(description))

    }

    @Test(expected = IllegalArgumentException::class)
    fun setInvalidNameTest() {
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, itemId, description)

        val invalidName = "    "

        validUser.setName(invalidName)

    }

    @Test
    fun setValidNameTest(){
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, itemId, description)

        val validName = "Bag"

        validUser.setName(validName)

        assertThat(validUser.getName(), Is(validName))
    }

    @Test
    fun setDescriptionTest(){
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, itemId, description)

        val newDescription = "Black wallet"

        validUser.setDescription(newDescription)

        assertThat(validUser.getDescription(), Is(newDescription))
    }

}







package com.github.brugapp.brug

import com.github.brugapp.brug.model.Item
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is

class ItemTest {

    @Test(expected = IllegalArgumentException::class)
    fun invalidNameItem() {
        val blankName = "    "
        val invalidUser = Item(blankName, 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyNameItem() {
        val emptyName = ""
        val invalidUser = Item(emptyName, 1)
    }

    @Test
    fun validNameItem(){
        val itemName = "Wallet"
        val itemId = 1
        val validUser = Item(itemName, itemId)

        assertThat(validUser.getName(), Is(itemName))


    }

    @Test
    fun validIdItem(){
        val itemName = "Wallet"
        val itemId = 1
        val validUser = Item(itemName, itemId)

        assertThat(validUser.getId(), Is(itemId))

    }

    @Test(expected = IllegalArgumentException::class)
    fun setInvalidName() {
        val itemName = "Wallet"
        val itemId = 1
        val validUser = Item(itemName, itemId)

        val invalidName = "    "

        validUser.setName(invalidName)

    }

    @Test
    fun setValidName(){
        val itemName = "Wallet"
        val itemId = 1
        val validUser = Item(itemName, itemId)

        val validName = "Bag"


        validUser.setName(validName)

        assertThat(validUser.getName(), Is(validName))
    }




}







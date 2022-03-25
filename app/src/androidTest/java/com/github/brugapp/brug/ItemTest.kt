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
        Item(blankName, R.drawable.ic_baseline_add_24, description, 1)

    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyNameItemTest() {
        val emptyName = ""
        val description = "Grey wallet"
        Item(emptyName, R.drawable.ic_baseline_add_24, description, 1)

    }

    @Test
    fun validNameItemTest(){
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, R.drawable.ic_baseline_add_24, description, itemId)

        assertThat(validUser.getName(), Is(itemName))

    }

    @Test
    fun validIdItemTest(){
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, R.drawable.ic_baseline_add_24, description, itemId)

        assertThat(validUser.getId(), Is(itemId))

    }

    @Test
    fun descriptionTest(){
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, R.drawable.ic_baseline_add_24, description, itemId)

        assertThat(validUser.getDescription(), Is(description))

    }

    @Test(expected = IllegalArgumentException::class)
    fun setInvalidNameTest() {
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, R.drawable.ic_baseline_add_24, description, itemId)

        val invalidName = "    "

        validUser.setName(invalidName)

    }

    @Test
    fun setValidNameTest(){
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, R.drawable.ic_baseline_add_24, description, itemId)

        val validName = "Bag"

        validUser.setName(validName)

        assertThat(validUser.getName(), Is(validName))
    }

    @Test
    fun setDescriptionTest(){
        val itemName = "Wallet"
        val itemId = 1
        val description = "Grey wallet"
        val validUser = Item(itemName, R.drawable.ic_baseline_add_24, description, itemId)

        val newDescription = "Black wallet"

        validUser.setDescription(newDescription)

        assertThat(validUser.getDescription(), Is(newDescription))
    }

}







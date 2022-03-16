package com.github.brugapp.brug

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.github.brugapp.brug.AddItemActivity.Companion.DESCRIPTION_LIMIT
import com.github.brugapp.brug.fake.MockDatabase.Companion.currentUser
import com.github.brugapp.brug.model.Item
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.*


import org.hamcrest.Matchers.*

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AddItemTest {

    @get:Rule
    var testRule = ActivityScenarioRule(AddItemActivity::class.java)

    @Test
    fun spinnerItemTest(){

        val bagSpinnerIndex = 1
        val bagItemName = "Bag"

        val itemTypeSpinner = onView(withId(R.id.itemTypeSpinner))
        itemTypeSpinner.perform(click())
        onData(anything()).atPosition(bagSpinnerIndex).perform(click())
        itemTypeSpinner.check(matches(withSpinnerText(containsString(bagItemName))))

    }

    @Test
    fun nameTest(){
        val itemName = "Bag"
        val itemNameText = onView(withId(R.id.itemName))
        itemNameText.perform(typeText(itemName)).check(matches(ViewMatchers.withText(itemName)))


        //itemNameText.check(matches(ViewMatchers.withText(itemName)))
    }

    @Test
    fun validDescriptionTest(){
        val description = "Grey Easpak backpack, with a laptop and an Ipad in it"
        val itemDescription = onView(withId(R.id.itemDescription))
        itemDescription.perform(typeText(description)).check(matches(ViewMatchers.withText(description)))

        //itemDescription.check(matches(ViewMatchers.withText(description)))
    }

    @Test
    fun tooLongDescriptionTest(){
        val longDescription = "Grey Easpak backpack, with a laptop and an Ipad in it, test test test test test"
        val expectedDescription = longDescription.take(DESCRIPTION_LIMIT)
        val itemDescription = onView(withId(R.id.itemDescription))
        itemDescription.perform(typeText(longDescription)).check(matches(ViewMatchers.withText(expectedDescription)))

        //itemDescription.check(matches(ViewMatchers.withText(expectedDescription)))
    }

    @Test
    fun nameHelperTextTest(){

        val emptyName = ""
        val itemName = onView(withId(R.id.itemName))
        val nameHelperText = onView(withId(R.id.itemNameHelper))
        itemName.perform(typeText(emptyName))

        val expectedHelperText = "Name must contain at least 1 character"

        val addButton = onView(withId(R.id.add_item_button))
        addButton.perform(click())

        // Verify that the Helper text changed after invalid name, and hence we are still in the AddItem activity
        nameHelperText.check(matches(ViewMatchers.withText(expectedHelperText)))
    }

    @Test
    fun validNameTextTest(){

        val validName = "Wallet"
        val itemName = onView(withId(R.id.itemName))
        itemName.perform(typeText(validName))

        /* Added the following two lines to make sure the keyboard is closed when we switch to ItemMenu activity,
           in order not to get a SecurityException
        */
        itemName.perform(closeSoftKeyboard())

        Intents.init()
        onView(withId(R.id.add_item_button)).perform(click())

        // Verify that the app goes to the Item List activity if the User enters valid info for his/her new item.
        Intents.intended(
            allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                IntentMatchers.hasComponent(ItemsMenuActivity::class.java.name)
            )
        )
        Intents.release()

    }

    //JUnit test to move to JUnit tests
    @Test
    fun itemAddedAfterValidForm(){

        val validName = "Wallet"
        val itemName = onView(withId(R.id.itemName))
        itemName.perform(typeText(validName))

        /* Added the following two lines to make sure the keyboard is closed when we switch to ItemMenu activity,
           in order not to get a SecurityException
        */
        itemName.perform(closeSoftKeyboard())

        onView(withId(R.id.add_item_button)).perform(click())

        val itemDescription = onView(withId(R.id.itemDescription))
        val itemId = 1

        val newItem = Item(itemName.toString(), itemId, itemDescription.toString())
        var itemList = ArrayList<Item>()
        itemList.add(newItem)

        //verify that the added item
        assertThat(currentUser.getItemList()[0].getId(), Is(newItem.getId()))
    }


}
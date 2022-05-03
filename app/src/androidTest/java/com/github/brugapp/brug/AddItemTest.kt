package com.github.brugapp.brug

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.ui.AddItemActivity
import com.github.brugapp.brug.ui.DESCRIPTION_LIMIT
import com.github.brugapp.brug.ui.ItemsMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//TODO: TRY TO PUT IMAGE ASSERTIONS NOW
private const val TEST_USER_UID = "TwSXfeusCKN95UvlGgY4uvEnXpl2"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddItemTest {

    @get:Rule
    var testRule = ActivityScenarioRule(AddItemActivity::class.java)

    @get:Rule
    var rule = HiltAndroidRule(this)

    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()

    private fun signInTestUser() {
        runBlocking {
            firebaseAuth.signInWithEmailAndPassword(
                "test@unlost.com",
                "123456").await()
        }
    }

    private fun wipeItemsAndSignOut() {
        runBlocking {
            ItemsRepository.deleteAllUserItems(TEST_USER_UID,firestore)
        }
        firebaseAuth.signOut()
    }

    @Before
    fun setUp() {
        Intents.init()
        signInTestUser()
    }

    @After
    fun cleanUp() {
        Intents.release()
        wipeItemsAndSignOut()
    }


    @Test
    fun spinnerItemTest(){
        val bagSpinnerIndex = 1
        val bagItemName = "Keys"

        val itemTypeSpinner = onView(withId(R.id.itemTypeSpinner))
        itemTypeSpinner.perform(click())
        onData(anything()).atPosition(bagSpinnerIndex).perform(click())
        itemTypeSpinner.check(matches(withSpinnerText(containsString(bagItemName))))

    }

    @Test
    fun nameTest(){
        val itemName = "Bag"
        val itemNameText = onView(withId(R.id.itemName))
        itemNameText.perform(typeText(itemName)).check(matches(withText(itemName)))
    }

    @Test
    fun validDescriptionTest(){
        val description = "Grey Easpak backpack, with a laptop and an Ipad in it"
        val itemDescription = onView(withId(R.id.itemDescription))
        itemDescription.perform(typeText(description)).check(matches(withText(description)))
    }

    @Test
    fun tooLongDescriptionTest(){
        val longDescription = "Grey Easpak backpack, with a laptop and an Ipad in it, test test test test test"
        val expectedDescription = longDescription.take(DESCRIPTION_LIMIT)
        val itemDescription = onView(withId(R.id.itemDescription))
        itemDescription.perform(typeText(longDescription)).check(matches(withText(expectedDescription)))
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
        nameHelperText.check(matches(withText(expectedHelperText)))
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
        onView(withId(R.id.add_item_button)).perform(click())

        // Verify that the app goes to the Item List activity if the User enters valid info for his/her new item.
        Intents.intended(
            allOf(
                IntentMatchers.toPackage("com.github.brugapp.brug"),
                hasComponent(ItemsMenuActivity::class.java.name)
            )
        )

    }

    //JUnit test to move to JUnit tests
  //  @Test
  //  fun itemAddedAfterValidForm(){
  //      val validName = "Wallet"
  //      val itemName = onView(withId(R.id.itemName))
  //      itemName.perform(typeText(validName))

  //      /* Added the following two lines to make sure the keyboard is closed when we switch to ItemMenu activity,
  //         in order not to get a SecurityException
  //      */
  //      itemName.perform(closeSoftKeyboard())

  //      onView(withId(R.id.add_item_button)).perform(click())

  //      onView(withId(R.id.items_listview)).check(matches(
  //          hasItemAtPosition(0, hasDescendant(
  //              withText(validName)
  //          ))))
  //  }

    //TODO: BE ABLE TO COMPARE TWO DRAWABLE IMAGES
    @Test
    fun addWalletCorrectIcon(){
        val validName = "Wallet"
        val itemName = onView(withId(R.id.itemName))
        itemName.perform(typeText(validName))
        itemName.perform(closeSoftKeyboard())
        val itemTypeSpinner = onView(withId(R.id.itemTypeSpinner))
        itemTypeSpinner.perform(click())
        onData(anything()).atPosition(ItemType.Wallet.ordinal).perform(click())
        onView(withId(R.id.add_item_button)).perform(click())

        // NEED TO TEST ON THE DISPLAYED LIST SINCE WE HAVE NO WAY
        // TO ACCESS THE USER'S ITEMS OUTSIDE THE NEW ACTIVITY
//        onView(withId(R.id.items_listview)).check(matches(
//            hasItemAtPosition(0, hasDescendant(
//                withDrawable(R.drawable.ic_baseline_account_balance_wallet_24)
//            ))))
    }

    @Test
    fun addKeysCorrectIcon(){
        val validName = "Keys"
        val itemName = onView(withId(R.id.itemName))
        itemName.perform(typeText(validName))
        itemName.perform(closeSoftKeyboard())
        val itemTypeSpinner = onView(withId(R.id.itemTypeSpinner))
        itemTypeSpinner.perform(click())
        onData(anything()).atPosition(ItemType.Keys.ordinal).perform(click())
        onView(withId(R.id.add_item_button)).perform(click())

//        assertThat(DUMMY_USER.getItemList().last().getRelatedIcon(), Is(R.drawable.ic_baseline_vpn_key_24))
    }

    @Test
    fun addCarKeysCorrectIcon(){
        val validName = "Car Keys"
        val itemName = onView(withId(R.id.itemName))
        itemName.perform(typeText(validName))
        itemName.perform(closeSoftKeyboard())
        val itemTypeSpinner = onView(withId(R.id.itemTypeSpinner))
        itemTypeSpinner.perform(click())
        onData(anything()).atPosition(ItemType.CarKeys.ordinal).perform(click())
        onView(withId(R.id.add_item_button)).perform(click())
//        assertThat(DUMMY_USER.getItemList().last().getRelatedIcon(), Is(R.drawable.ic_baseline_car_rental_24))
    }

    @Test
    fun addPhoneCorrectIcon(){
        val validName = "BigBoyPhone"
        val itemName = onView(withId(R.id.itemName))
        itemName.perform(typeText(validName))
        itemName.perform(closeSoftKeyboard())
        val itemTypeSpinner = onView(withId(R.id.itemTypeSpinner))
        itemTypeSpinner.perform(click())
        onData(anything()).atPosition(ItemType.Phone.ordinal).perform(click())
        onView(withId(R.id.add_item_button)).perform(click())
//        assertThat(DUMMY_USER.getItemList().last().getRelatedIcon(), Is(R.drawable.ic_baseline_smartphone_24))
    }

    @Test
    fun addOtherCorrectIcon(){
        val validName = "Dunno"
        val itemName = onView(withId(R.id.itemName))
        itemName.perform(typeText(validName))
        itemName.perform(closeSoftKeyboard())
        val itemTypeSpinner = onView(withId(R.id.itemTypeSpinner))
        itemTypeSpinner.perform(click())
        onData(anything()).atPosition(ItemType.Other.ordinal).perform(click())
        onView(withId(R.id.add_item_button)).perform(click())
//        assertThat(DUMMY_USER.getItemList().last().getRelatedIcon(), Is(R.drawable.ic_baseline_add_24))
    }


    // TO MATCH IMAGES INSIDE ITEM LIST
    private fun hasItemAtPosition(position: Int, matcher: Matcher<View>): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

            override fun describeTo(description: Description?) {
                description?.appendText("has item at position $position : ")
                matcher.describeTo(description)
            }

            override fun matchesSafely(recyclerView: RecyclerView): Boolean {
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                    ?: return false
                return matcher.matches(viewHolder.itemView)
            }
        }
    }

    private fun withDrawable(@DrawableRes id: Int) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("$id")
        }

        override fun matchesSafely(view: View): Boolean {
            val context = view.context
            val expectedBitmap = context.getDrawable(id)?.toBitmap()

            return view is ImageView && view.drawable.toBitmap().sameAs(expectedBitmap)
        }
    }

}
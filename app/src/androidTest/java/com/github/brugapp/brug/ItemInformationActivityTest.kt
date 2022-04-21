package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.fake.MockDatabase
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.MyItem
import com.github.brugapp.brug.ui.ItemInformationActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemInformationActivityTest{
    private val str = "no information yet"
    private val item = MyItem("Phone", ItemType.Phone.ordinal, "Samsung Galaxy S22", false)

    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), ItemInformationActivity::class.java)
        intent.putExtra(ITEM_INTENT_KEY, item)
        ActivityScenario.launch<ItemInformationActivity>(intent)
    }

    @Test
    fun correctTypeDisplayed(){
        onView(ViewMatchers.withId(R.id.item_name)).check(matches(withText("Phone")))
        onView(ViewMatchers.withId(R.id.tv_name)).check(matches(withText("Phone")))
    }

    @Test
    fun correctDescriptionDisplayed(){
        onView(ViewMatchers.withId(R.id.item_description)).check(matches(withText("Samsung Galaxy S22")))
    }

    @Test
    fun noLocationAndOwnerAndDateYet(){
        onView(ViewMatchers.withId(R.id.item_last_location)).check(matches(withText(str)))
        onView(ViewMatchers.withId(R.id.item_owner)).check(matches(withText(str)))
    }

    @Test
    fun declaredItemAsLost(){
        runBlocking { Firebase.auth.signInWithEmailAndPassword(
            "test@unlost.com",
            "123456"
        ) }
        assertThat(
            item.isLost(),
            CoreMatchers.`is`(false)
        )


        Firebase.auth.signOut()

//        onView(ViewMatchers.withId(R.id.isLostSwitch)).perform(click())
//        assertThat(
//            item.isLost(),
//            CoreMatchers.`is`(true)
//        )
//
    }
}
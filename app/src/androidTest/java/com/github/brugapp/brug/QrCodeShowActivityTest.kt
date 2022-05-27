package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.model.ItemType
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.ui.QrCodeShowActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeShowActivityTest{
    private val item = Item("Phone", ItemType.Phone.ordinal,"my phone", false)
    private val code:String = "0:23"
    private val user= User("0","Brug", "App",null, mutableListOf())

    private val infoListIntent = Intent(
        ApplicationProvider.getApplicationContext(),
        QrCodeShowActivity::class.java
    ).apply {
        item.setItemID("23")
        putExtra("qrId", user.uid+":"+item.getItemID())
    }

    @Test
    fun correctQrIsShown(){
        ActivityScenario.launch<QrCodeShowActivity>(infoListIntent).use {
            onView(ViewMatchers.withId(R.id.itemNameShow)).check(matches(withText(code)))
        }
    }
}


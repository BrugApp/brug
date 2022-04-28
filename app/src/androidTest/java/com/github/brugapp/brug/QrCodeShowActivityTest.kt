package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.ui.QrCodeShowActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeShowActivityTest{
    private val item = Item("Phone","Samsung Galaxy S22","23")
    private val code:String = "0:23"
    private val user= User("Brug","App","wagwan@email.ch","0",null)

    private val infoListIntent = Intent(
        ApplicationProvider.getApplicationContext(),
        QrCodeShowActivity::class.java
    ).apply {
        putExtra("qrId", user.getId()+":"+item.getId())
    }

    @Test
    fun correctQrIsShown(){
        ActivityScenario.launch<QrCodeShowActivity>(infoListIntent).use {
            onView(ViewMatchers.withId(R.id.codeId)).check(matches(withText(code)))
        }
    }

}
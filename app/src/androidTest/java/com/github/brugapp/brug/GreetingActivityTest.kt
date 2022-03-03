package com.github.brugapp.brug

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GreetingActivityTest {

    @Test
    fun greetingActivityGreetsWithCorrectName() {

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            GreetingActivity::class.java
        ).apply {
            putExtra(EXTRA_NAME, "Goku")
        }

        ActivityScenario.launch<GreetingActivity>(intent).use { scenario ->
            onView(ViewMatchers.withId(R.id.greetingMessage)).check(matches(withText("Hello, Goku!")))
        }

    }
}
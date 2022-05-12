package com.github.brugapp.brug;
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.ui.FullScreenImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class FullScreenImageTest {
    @get:Rule
    var fullScreenImageTestRule = ActivityScenarioRule(FullScreenImage::class.java)

    @Test
    fun imageViewHasUrl() {
        Espresso.onView(ViewMatchers.withId(R.id.selectedImage))
            .check(ViewAssertions.matches(ViewMatchers.withContentDescription("Image displayed in fullscreen")))
    }
}

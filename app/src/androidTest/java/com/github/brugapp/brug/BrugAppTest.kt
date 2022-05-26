package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BrugAppTest {
    @get:Rule
    val rule = HiltAndroidRule(this)

    @Test
    fun launchingBrugAppDoesNotCrashTest() {
          BrugApp()
    }

}
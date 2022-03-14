package com.github.brugapp.brug

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatMenuActivityTest {
    @get:Rule
    val testRule = ActivityScenarioRule(ChatMenuActivity::class.java)

}
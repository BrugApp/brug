package com.github.brugapp.brug.sign_in

import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.brugapp.brug.R
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.di.sign_in.SignInClient
import com.github.brugapp.brug.di.sign_in.module.SignInAccountModule
import com.github.brugapp.brug.fake.FakeGoogleSignInAccount
import com.github.brugapp.brug.fake.FakeSignInClient
import com.github.brugapp.brug.ui.ItemsMenuActivity
import com.github.brugapp.brug.ui.SignInActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UninstallModules(SignInAccountModule::class)
@HiltAndroidTest
class SignInActivityTestFakeGoogle {

    @Module
    @InstallIn(ViewModelComponent::class)
    object FakeSignInAccountModule {
        @ViewModelScoped
        @Provides
        fun provideFakeSignInAccount(): SignInAccount {
            return FakeGoogleSignInAccount(GoogleSignInAccount.createDefault())
        }
    }

    @get:Rule
    val rule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        ActivityScenario.launch<SignInActivity>(
            Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)
        )
    }


    @Test
    fun signInActivityFailsForDefaultSignedInUser() {

        onView(withId(R.id.qr_found_btn))
            .check(matches(isDisplayed()))
        // check if contains sign out button
        onView(withId(R.id.sign_in_google_button))
            .check(matches(isDisplayed()))

    }
}
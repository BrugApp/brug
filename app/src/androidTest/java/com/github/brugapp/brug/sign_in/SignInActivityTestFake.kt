package com.github.brugapp.brug.sign_in

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.github.brugapp.brug.R
import com.github.brugapp.brug.di.sign_in.AuthDatabase
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.di.sign_in.SignInClient
import com.github.brugapp.brug.di.sign_in.module.DatabaseAuthModule
import com.github.brugapp.brug.di.sign_in.module.SignInAccountModule
import com.github.brugapp.brug.di.sign_in.module.SignInClientModule
import com.github.brugapp.brug.fake.FakeAuthDatabase
import com.github.brugapp.brug.fake.FakeDatabaseUser
import com.github.brugapp.brug.fake.FakeSignInAccount
import com.github.brugapp.brug.fake.FakeSignInClient
import com.github.brugapp.brug.ui.SignInActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test

@UninstallModules(SignInAccountModule::class, SignInClientModule::class, DatabaseAuthModule::class)
@HiltAndroidTest
class SignInActivityTestFake {

    // Inject Fake dependencies
    @Module
    @InstallIn(ViewModelComponent::class)
    object FakeSignInClientModule {
        @ViewModelScoped
        @Provides
        fun provideFakeSignInClient(): SignInClient {
            return FakeSignInClient()
        }
    }

    @Module
    @InstallIn(ViewModelComponent::class)
    object FakeSignInAccountModule {
        @ViewModelScoped
        @Provides
        fun provideFakeSignInAccount(): SignInAccount {
            return FakeSignInAccount()
        }
    }

    @Module
    @InstallIn(ViewModelComponent::class)
    object FakeAuthDatabase {

        @ViewModelScoped
        @Provides
        fun provideFakeAuthDatabase(): AuthDatabase {
            return FakeAuthDatabase(FakeDatabaseUser())
        }
    }

    @get:Rule
    val rule = HiltAndroidRule(this)

    @Test
    fun signInActivityWelcomesWithCorrectDisplayNameAndSignOutButtonForSignedInUser() {

        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            // check if displays correct display name
            onView(withId(R.id.sign_in_main_text))
                .check(
                    matches(
                        withText("Welcome Son Goku\nEmail: goku@capsulecorp.com")
                    )
                )
            // check if contains sign out button
            onView(withId(R.id.sign_in_sign_out_button))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun signInActivityAsksUserToSignInForNotSignedInUser() {

        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            // sign out
            onView(withId(R.id.sign_in_sign_out_button))
                .perform(click())
            // check if displays correct message
            onView(withId(R.id.sign_in_main_text))
                .check(matches(withText("Sign in to Unlost")))
            // check if contains sign in button
            onView(withId(R.id.sign_in_google_button))
                .check(matches(isDisplayed()))
        }
    }

}

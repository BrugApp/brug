package com.github.brugapp.brug.sign_in

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
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
import com.github.brugapp.brug.ui.ItemsMenuActivity
import com.github.brugapp.brug.ui.SignInActivity
import com.google.firebase.auth.ktx.auth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
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
            return FakeSignInAccount("Son Goku", "Vegeta", "0", "goku@capsulecorp.com")
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

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

//    @Test
//    fun signInActivityGoesToCorrectActivityForSignedInUser() {
//
//        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)
//
//        ActivityScenario.launch<SignInActivity>(intent).use {
//            intended(
//                hasComponent(
//                    hasClassName(
//                        ItemsMenuActivity::class.java.name
//                    )
//                )
//            )
//        }
//    }

//    @Test
//    fun signInActivitySignsOutCorrectlyFromSettings() {
//
//        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)
//
//        ActivityScenario.launch<SignInActivity>(intent).use {
//
//            val settingsButton = onView(withId(R.id.my_settings))
//            settingsButton.perform(click())
//            onView(withId(R.id.sign_out_button)).perform(click())
//
//            // check if contains guest button
//            onView(withId(R.id.qr_found_btn))
//                .check(matches(isDisplayed()))
//            // check if contains sign out button
//            onView(withId(R.id.sign_in_google_button))
//                .check(matches(isDisplayed()))
//        }
//    }

}

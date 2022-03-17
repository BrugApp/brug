package com.github.brugapp.brug.sign_in

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.github.brugapp.brug.R
import com.github.brugapp.brug.di.sign_in.*
import com.github.brugapp.brug.fake.FakeAuthDatabase
import com.github.brugapp.brug.fake.FakeSignInCredentialGetter
import com.github.brugapp.brug.fake.FakeSignInResultHandler
import com.github.brugapp.brug.ui.SignInActivity
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

@UninstallModules(
    SignInResultHandlerModule::class,
    DatabaseAuthModule::class,
    SignInCredentialGetterModule::class
)
@HiltAndroidTest
class SignInActivityTestFakeHandler {

    @Module
    @InstallIn(ViewModelComponent::class)
    object FakeSignInResultHandler {

        @ViewModelScoped
        @Provides
        fun provideFakeSignInResultHandler(): SignInResultHandler {
            return FakeSignInResultHandler()
        }
    }

    @Module
    @InstallIn(ViewModelComponent::class)
    object FakeAuthDatabase {

        @ViewModelScoped
        @Provides
        fun provideFakeAuthDatabase(): AuthDatabase {
            return FakeAuthDatabase(null)
        }
    }

    @Module
    @InstallIn(ViewModelComponent::class)
    object FakeCredentialGetterModule {
        @ViewModelScoped
        @Provides
        fun provideFakeGoogleCredentialGetter(): SignInCredentialGetter {
            return FakeSignInCredentialGetter()
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


    @Test
    fun signInActivityAsksUserToSignInForNotSignedInUser() {

        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            // check if displays correct message
            onView(withId(R.id.sign_in_main_text))
                .check(matches(withText("Sign in to Unlost")))
            // check if contains sign in button
            onView(withId(R.id.sign_in_google_button))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun activityResultOKTest() {

        Intents.intending(
            IntentMatchers.hasComponent(
                ComponentNameMatchers.hasClassName(
                    SignInActivity::class.java.name
                )
            )
        )
            .respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    Intent()
                )
            )

        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)
        ActivityScenario.launch<SignInActivity?>(intent).use {
            it.onActivity { activity ->
                activity.getSignInResult.launch(Intent(activity.intent))
            }

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

            // sign out
            onView(withId(R.id.sign_in_sign_out_button)).perform(click())
        }

    }

    @Test
    fun activityResultCanceledTest() {

        Intents.intending(
            IntentMatchers.hasComponent(
                ComponentNameMatchers.hasClassName(
                    SignInActivity::class.java.name
                )
            )
        )
            .respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_CANCELED,
                    Intent()
                )
            )

        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)
        ActivityScenario.launch<SignInActivity?>(intent).use {
            it.onActivity { activity ->
                activity.getSignInResult.launch(Intent(activity.intent))
            }

            // check if displays correct message
            onView(withId(R.id.sign_in_main_text))
                .check(matches(withText("Sign in to Unlost")))
            // check if contains sign in button
            onView(withId(R.id.sign_in_google_button))
                .check(matches(isDisplayed()))
        }

    }

    @Test
    fun clickOnGoogleSignInButtonDoesNotCrash() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)


        ActivityScenario.launch<SignInActivity>(intent).use {
            onView(withId(R.id.sign_in_google_button)).perform(click())
        }
    }
}
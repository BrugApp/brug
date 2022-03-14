package com.github.brugapp.brug

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.github.brugapp.brug.fake.FakeGoogleSignInAccount
import com.github.brugapp.brug.fake.FakeSignInAccount
import com.github.brugapp.brug.fake.FakeSignInClient
import com.github.brugapp.brug.fake.FakeSignInResultHandler
import com.github.brugapp.brug.sign_in.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test


@UninstallModules(SignInAccountModule::class, SignInClientModule::class)
@HiltAndroidTest
class SignInActivityTestFake {

    // Inject Fake dependencies
    @Module
    @InstallIn(ActivityComponent::class)
    object FakeSignInClientModule {
        @ActivityScoped
        @Provides
        fun provideFakeSignInClient(): SignInClient {
            return FakeSignInClient()
        }
    }

    @Module
    @InstallIn(ActivityComponent::class)
    object FakeSignInAccountModule {
        @ActivityScoped
        @Provides
        fun provideFakeSignInAccount(): SignInAccount {
            return FakeSignInAccount()
        }
    }


    @get:Rule
    var rule = HiltAndroidRule(this)

    @Test
    fun signInActivityWelcomesWithCorrectDisplayNameAndSignOutButtonForSignedInUser() {

        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            // check if displays correct display name
            Espresso.onView(withId(R.id.sign_in_main_text))
                .check(ViewAssertions.matches(ViewMatchers.withText("Welcome Son Goku\nEmail: goku@capsulecorp.com")))
            // check if contains sign out button
            Espresso.onView(withId(R.id.sign_in_sign_out_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }

    @Test
    fun signInActivityAsksUserToSignInForNotSignedInUser() {

        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            // sign out
            Espresso.onView(withId(R.id.sign_in_sign_out_button)).perform(ViewActions.click())
            // check if displays correct message
            Espresso.onView(withId(R.id.sign_in_main_text))
                .check(ViewAssertions.matches(ViewMatchers.withText("Sign in to Unlost")))
            // check if contains sign in button
            Espresso.onView(withId(R.id.sign_in_google_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }

}


@UninstallModules(SignInResultHandlerModule::class)
@HiltAndroidTest
class SignInActivityTestFakeHandler {

    @Module
    @InstallIn(ActivityComponent::class)
    object FakeSignInResultHandler {

        @ActivityScoped
        @Provides
        fun provideFakeSignInResultHandler(): SignInResultHandler {
            return FakeSignInResultHandler()
        }
    }

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Test
    fun signInActivityAsksUserToSignInForNotSignedInUser() {

        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            // check if displays correct message
            Espresso.onView(withId(R.id.sign_in_main_text))
                .check(ViewAssertions.matches(ViewMatchers.withText("Sign in to Unlost")))
            // check if contains sign in button
            Espresso.onView(withId(R.id.sign_in_google_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }

    @Test
    fun activityResultOKTest() {

        Intents.init()

        intending(hasComponent(hasClassName(SignInActivity::class.java.name)))
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
            Espresso.onView(withId(R.id.sign_in_main_text))
                .check(ViewAssertions.matches(ViewMatchers.withText("Welcome Son Goku\nEmail: goku@capsulecorp.com")))
            // check if contains sign out button
            Espresso.onView(withId(R.id.sign_in_sign_out_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

            // sign out
            Espresso.onView(withId(R.id.sign_in_sign_out_button)).perform(ViewActions.click())
        }

        Intents.release()
    }

    @Test
    fun activityResultCanceledTest() {

        Intents.init()

        intending(hasComponent(hasClassName(SignInActivity::class.java.name)))
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
            Espresso.onView(withId(R.id.sign_in_main_text))
                .check(ViewAssertions.matches(ViewMatchers.withText("Sign in to Unlost")))
            // check if contains sign in button
            Espresso.onView(withId(R.id.sign_in_google_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }

        Intents.release()
    }

    @Test
    fun clickOnGoogleSignInButtonDoesNotCrash() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            Espresso.onView(withId(R.id.sign_in_google_button)).perform(ViewActions.click())
        }
    }
}

@UninstallModules(SignInAccountModule::class)
@HiltAndroidTest
class SignInActivityTestFakeGoogle {

    @Module
    @InstallIn(ActivityComponent::class)
    object FakeSignInAccountModule {
        @ActivityScoped
        @Provides
        fun provideFakeSignInAccount(): SignInAccount {
            return FakeGoogleSignInAccount(GoogleSignInAccount.createDefault())
        }
    }

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Test
    fun signInActivityWelcomesWithCorrectDisplayNameAndSignOutButtonForSignedInUser() {

        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            // check if displays correct display name
            Espresso.onView(withId(R.id.sign_in_main_text))
                .check(ViewAssertions.matches(ViewMatchers.withText("Welcome null\nEmail: <<default account>>")))
            // check if contains sign out button
            Espresso.onView(withId(R.id.sign_in_sign_out_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }

    }


}

@HiltAndroidTest
class SignInActivityTestRealGoogle {

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Test
    fun signInActivityGetsNullAccountOnFakeResult() {

        Intents.init()

        intending(hasComponent(hasClassName(SignInActivity::class.java.name)))
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
            Espresso.onView(withId(R.id.sign_in_main_text))
                .check(ViewAssertions.matches(ViewMatchers.withText("Welcome null\nEmail: null")))
            // check if contains sign out button
            Espresso.onView(withId(R.id.sign_in_sign_out_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }

        Intents.release()

    }


}
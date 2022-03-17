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
import com.github.brugapp.brug.di.sign_in.*
import com.github.brugapp.brug.fake.*
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
    object FakeAuthDatabse {

        @ViewModelScoped
        @Provides
        fun provideFakeAuthDatabase(): AuthDatabase {
            return FakeAuthDatabase(FakeDatabaseUser())
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
                .check(
                    ViewAssertions.matches(
                        ViewMatchers.withText("Welcome Son Goku\nEmail: goku@capsulecorp.com")
                    )
                )
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
    var rule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun finishUp() {
        Intents.release()
    }


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
                .check(
                    ViewAssertions.matches(
                        ViewMatchers.withText("Welcome Son Goku\nEmail: goku@capsulecorp.com")
                    )
                )
            // check if contains sign out button
            Espresso.onView(withId(R.id.sign_in_sign_out_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

            // sign out
            Espresso.onView(withId(R.id.sign_in_sign_out_button)).perform(ViewActions.click())
        }

    }

    @Test
    fun activityResultCanceledTest() {

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
    @InstallIn(ViewModelComponent::class)
    object FakeSignInAccountModule {
        @ViewModelScoped
        @Provides
        fun provideFakeSignInAccount(): SignInAccount {
            return FakeGoogleSignInAccount(GoogleSignInAccount.createDefault())
        }
    }

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Test
    fun signInActivityDisplaysSignInPromptForNullSignedInUser() {

        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)

        ActivityScenario.launch<SignInActivity>(intent).use {
            // check if displays correct display name
            Espresso.onView(withId(R.id.sign_in_main_text))
                .check(ViewAssertions.matches(ViewMatchers.withText("Sign in to Unlost")))
            // check if contains sign out button
            Espresso.onView(withId(R.id.sign_in_google_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }

    }


}

@UninstallModules(SignInCredentialGetterModule::class)
@HiltAndroidTest
class SignInActivityTestRealGoogle {

    @Module
    @InstallIn(ViewModelComponent::class)
    object FakeCredentialGetterModule {
        @ViewModelScoped
        @Provides
        fun provideFakeGoogleCredentialGetter(): SignInCredentialGetter {
            return FakeGoogleSignInCredentialGetter()
        }
    }

    @get:Rule
    var rule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    @Test
    fun signInActivityGetsNullAccountOnFakeResult() {

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
                .check(ViewAssertions.matches(ViewMatchers.withText("Sign in to Unlost")))
            // check if contains sign out button
            Espresso.onView(withId(R.id.sign_in_google_button))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }
}
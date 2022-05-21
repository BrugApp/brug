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
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.github.brugapp.brug.R
import com.github.brugapp.brug.di.sign_in.AuthDatabase
import com.github.brugapp.brug.di.sign_in.SignInCredentialGetter
import com.github.brugapp.brug.di.sign_in.SignInResultHandler
import com.github.brugapp.brug.di.sign_in.module.DatabaseAuthModule
import com.github.brugapp.brug.di.sign_in.module.SignInCredentialGetterModule
import com.github.brugapp.brug.di.sign_in.module.SignInResultHandlerModule
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
            onView(withId(R.id.qr_found_btn))
                .check(matches(isDisplayed()))
            // check if contains sign in button
            onView(withId(R.id.sign_in_google_button))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun activityResultOKTest() {
        intending(
            hasComponent(
                ComponentNameMatchers.hasClassName(
                    SignInActivity::class.java.name
                )
            )
        ).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                Intent()
            )
        )

        //TODO: UNCOMMENT WHEN SIGNIN PROCEDURE IS FINALLY FUNCTIONAL
//        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)
//        ActivityScenario.launch<SignInActivity?>(intent).use {
//            it.onActivity { activity ->
//                activity.getSignInResult.launch(Intent(activity.intent))
//            }
//
//            intended(
//                hasComponent(
//                    ComponentNameMatchers.hasClassName(
//                        ItemsMenuActivity::class.java.name
//                    )
//                ))
//        }

    }

    @Test
    fun activityResultCanceledTest() {

        intending(
            hasComponent(
                ComponentNameMatchers.hasClassName(
                    SignInActivity::class.java.name
                )
            )
        ).respondWith(
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

            // check if contains guest button
            onView(withId(R.id.qr_found_btn))
                .check(matches(isDisplayed()))
            // check if contains sign in button
            onView(withId(R.id.sign_in_google_button))
                .check(matches(isDisplayed()))
        }

    }

//    @Test
//    fun clickOnGoogleSignInButtonDoesNotCrash() {
//        val intent = Intent(ApplicationProvider.getApplicationContext(), SignInActivity::class.java)
//
//
//        ActivityScenario.launch<SignInActivity>(intent).use {
//            Thread.sleep(1000)
//            onView(withId(R.id.sign_in_google_button)).perform(click())
//        }
//    }
}
package com.github.brugapp.brug.sign_in

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import com.github.brugapp.brug.R
import com.github.brugapp.brug.di.sign_in.SignInCredentialGetter
import com.github.brugapp.brug.di.sign_in.module.SignInCredentialGetterModule
import com.github.brugapp.brug.fake.FakeGoogleSignInCredentialGetter
import com.github.brugapp.brug.ui.ItemsMenuActivity
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
    fun signInActivityWorksOnFakeResult() {

        intending(
            hasComponent(
                hasClassName(
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

            Intents.intended(
                hasComponent(
                    hasClassName(
                        ItemsMenuActivity::class.java.name
                    )
                )
            )
        }
    }
}
package com.github.brugapp.brug.sign_in

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped


@Module
@InstallIn(ActivityComponent::class)
object SignInAccountModule {

    @ActivityScoped
    @Provides
    fun provideGoogleSignInAccount(@ApplicationContext context: Context): SignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        return SignInAccountGoogle(account)
    }
}
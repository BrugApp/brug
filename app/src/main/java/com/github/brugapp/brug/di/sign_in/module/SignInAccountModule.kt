package com.github.brugapp.brug.di.sign_in.module

import android.content.Context
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.di.sign_in.google.SignInAccountGoogle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object SignInAccountModule {

    @ViewModelScoped
    @Provides
    fun provideGoogleSignInAccount(@ApplicationContext context: Context): SignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        return SignInAccountGoogle(account)
    }
}
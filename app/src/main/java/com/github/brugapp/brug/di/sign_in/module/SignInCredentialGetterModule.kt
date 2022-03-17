package com.github.brugapp.brug.di.sign_in.module

import com.github.brugapp.brug.di.sign_in.SignInCredentialGetter
import com.github.brugapp.brug.di.sign_in.google.SignInCredentialGetterGoogle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object SignInCredentialGetterModule {

    @ViewModelScoped
    @Provides
    fun provideGoogleSignInResultHandler(): SignInCredentialGetter {
        return SignInCredentialGetterGoogle()
    }
}
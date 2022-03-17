package com.github.brugapp.brug.di.sign_in.module

import com.github.brugapp.brug.di.sign_in.SignInResultHandler
import com.github.brugapp.brug.di.sign_in.google.SignInResultHandlerGoogle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object SignInResultHandlerModule {

    @ViewModelScoped
    @Provides
    fun provideGoogleSignInResultHandler(): SignInResultHandler {
        return SignInResultHandlerGoogle()
    }
}
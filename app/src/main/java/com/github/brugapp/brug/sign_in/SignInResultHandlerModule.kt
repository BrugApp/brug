package com.github.brugapp.brug.sign_in

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object SignInResultHandlerModule {

    @ActivityScoped
    @Provides
    fun provideGoogleSignInResultHandler(): SignInResultHandler {
        return SignInResultHandlerGoogle()
    }
}
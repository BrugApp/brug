package com.github.brugapp.brug.sign_in

import android.content.Context
import com.github.brugapp.brug.SignInResultHandler
import com.github.brugapp.brug.SignInResultHandlerGoogle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object SignInResultHandlerModule {

    @ActivityScoped
    @Provides
    fun provideGoogleSignInResultHandler(@ApplicationContext context: Context): SignInResultHandler {
        return SignInResultHandlerGoogle()
    }
}
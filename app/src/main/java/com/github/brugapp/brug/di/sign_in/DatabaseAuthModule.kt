package com.github.brugapp.brug.di.sign_in

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object DatabaseAuthModule {

    @ViewModelScoped
    @Provides
    fun provideFirebaseAuth(): AuthDatabase {
        return AuthFirebase()
    }
}
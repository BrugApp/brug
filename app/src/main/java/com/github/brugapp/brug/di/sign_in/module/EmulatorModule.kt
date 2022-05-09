package com.github.brugapp.brug.di.sign_in.module

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent


@Module
@InstallIn(ActivityComponent::class)
object EmulatorModule {

    @Provides
    fun providesFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    fun providesStorage(): FirebaseStorage {
        return Firebase.storage
    }

    @Provides
    fun provideAuth(): FirebaseAuth {
        return Firebase.auth
    }
}
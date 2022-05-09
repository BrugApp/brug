package com.github.brugapp.brug.fake

import com.github.brugapp.brug.di.sign_in.module.EmulatorModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [ActivityComponent::class],
    replaces = [EmulatorModule::class]
)
object FakeEmulatorModule {

    @Provides
    fun providesFirestore(): FirebaseFirestore {
        return FirebaseFakeHelper().providesFirestore()
    }

    @Provides
    fun providesStorage(): FirebaseStorage {
        return FirebaseFakeHelper().providesStorage()
    }

    @Provides
    fun providesAuth(): FirebaseAuth {
        return FirebaseFakeHelper().providesAuth()
    }

}
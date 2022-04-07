package com.github.brugapp.brug.di.sign_in.module

import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
object ActivityResultModule {

    @Provides
    fun provideActivityResultRegistry(@ActivityContext activity: Context):ActivityResultRegistry =
        (activity as? AppCompatActivity)?.activityResultRegistry
            ?: throw IllegalArgumentException("You must use AppCompatActivity")
}
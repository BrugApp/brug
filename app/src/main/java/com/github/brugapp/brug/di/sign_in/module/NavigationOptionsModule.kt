package com.github.brugapp.brug.di.sign_in.module

import android.content.Context
import com.github.brugapp.brug.R
import com.mapbox.navigation.base.options.NavigationOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
object NavigationOptionsModule {

    @Provides
    fun providesNavigationOptions(@ActivityContext activity: Context): NavigationOptions {
        return NavigationOptions.Builder(activity.applicationContext)
            .accessToken(activity.getString(R.string.mapbox_access_token))
            .build()
    }
}
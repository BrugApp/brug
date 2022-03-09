package com.github.brugapp.brug.sign_in

import android.content.Context
import com.github.brugapp.brug.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped


@Module
@InstallIn(ActivityComponent::class)
object SignInClientModule {

    @ActivityScoped
    @Provides
    fun provideSignInClient(@ApplicationContext context: Context): SignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_client_id))
            .requestEmail()
            .build()

        return SignInClientGoogle(GoogleSignIn.getClient(context, gso))
    }

}

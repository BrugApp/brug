package com.github.brugapp.brug

import com.github.brugapp.brug.di.sign_in.module.EmulatorModule
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Test

class ModuleTest {

    @Test
    fun firebaseStartWithoutEmulator() {
        val auth = EmulatorModule.provideAuth()
        //chack that the auth is not null
        assertThat(auth, IsEqual.equalTo(FirebaseAuth.getInstance()))
    }
}
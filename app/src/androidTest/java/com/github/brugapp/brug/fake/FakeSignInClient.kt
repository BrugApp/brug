package com.github.brugapp.brug.fake

import android.content.Intent
import com.github.brugapp.brug.sign_in.SignInClient

class FakeSignInClient : SignInClient() {
    override val signInIntent: Intent
        get() = Intent()

    override fun signOut() {}
}
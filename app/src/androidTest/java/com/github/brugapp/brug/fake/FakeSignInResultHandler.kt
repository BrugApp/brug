package com.github.brugapp.brug.fake

import androidx.activity.result.ActivityResult
import com.github.brugapp.brug.SignInResultHandler
import com.github.brugapp.brug.sign_in.SignInAccount

class FakeSignInResultHandler : SignInResultHandler() {
    override fun handleSignInResult(result: ActivityResult): SignInAccount? {
        return FakeSignInAccount()
    }
}
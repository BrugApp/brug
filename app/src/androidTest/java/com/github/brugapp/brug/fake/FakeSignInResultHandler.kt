package com.github.brugapp.brug.fake

import androidx.activity.result.ActivityResult
import com.github.brugapp.brug.sign_in.SignInAccount
import com.github.brugapp.brug.sign_in.SignInResultHandler

class FakeSignInResultHandler : SignInResultHandler() {
    override fun handleSignInResult(result: ActivityResult): SignInAccount {
        return FakeSignInAccount()
    }
}
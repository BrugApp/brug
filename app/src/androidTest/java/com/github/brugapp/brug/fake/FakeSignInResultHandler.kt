package com.github.brugapp.brug.fake

import android.content.Intent
import com.github.brugapp.brug.data.BrugSignInAccount
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.di.sign_in.SignInResultHandler

class FakeSignInResultHandler : SignInResultHandler() {
    override fun handleSignInResult(result: Intent?): SignInAccount {
        return BrugSignInAccount("Son Goku", "Vegeta", "0", "goku@capsulecorp.com")
    }
}
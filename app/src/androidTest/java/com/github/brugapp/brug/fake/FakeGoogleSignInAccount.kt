package com.github.brugapp.brug.fake

import com.github.brugapp.brug.sign_in.SignInAccountGoogle
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class FakeGoogleSignInAccount(account: GoogleSignInAccount?) : SignInAccountGoogle(account)
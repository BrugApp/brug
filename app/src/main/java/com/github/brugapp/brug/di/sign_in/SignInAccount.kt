package com.github.brugapp.brug.di.sign_in


abstract class SignInAccount {

    abstract val firstName: String?
    abstract val lastName: String?
    abstract val idToken: String?
    abstract val email: String?
}
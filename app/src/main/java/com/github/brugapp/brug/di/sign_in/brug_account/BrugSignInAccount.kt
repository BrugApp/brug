package com.github.brugapp.brug.di.sign_in.brug_account

import com.github.brugapp.brug.di.sign_in.SignInAccount

/**
 * Sign In Account class used in the app
 *
 * @property fname first name
 * @property lname last name
 * @property token id
 * @property mail email
 */
class BrugSignInAccount(
    private val fname: String,
    private val lname: String,
    private val token: String,
    private val mail: String
) : SignInAccount() {
    override val firstName: String
        get() = fname
    override val lastName: String
        get() = lname
    override val idToken: String
        get() = token
    override val email: String
        get() = mail
}
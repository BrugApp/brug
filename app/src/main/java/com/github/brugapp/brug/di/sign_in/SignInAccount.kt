package com.github.brugapp.brug.di.sign_in

/**
 * Abstract Sign in account instance used in the app
 *
 */
abstract class SignInAccount {

    /**
     * First name
     */
    abstract val firstName: String?

    /**
     * Last name
     */
    abstract val lastName: String?

    /**
     * User id
     */
    abstract val idToken: String?

    /**
     * Email
     */
    abstract val email: String?
}
package com.github.brugapp.brug.di.sign_in

/**
 * Abstract Database User instance used in the app
 *
 */
abstract class DatabaseUser {
    /**
     * Display name
     */
    abstract val displayName: String?

    /**
     * Email
     */
    abstract val email: String?
}

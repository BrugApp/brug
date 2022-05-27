package com.github.brugapp.brug.di.sign_in


import com.google.firebase.auth.AuthCredential

/**
 * Abstract Authentication database instance used in the app
 *
 */
abstract class AuthDatabase {

    /**
     * Current signed in user
     */
    abstract val currentUser: DatabaseUser?

    /**
     * Sign out from database
     *
     */
    abstract fun signOut()

    /**
     * Signs in to Database with credential
     *
     * @param credential
     * @return User id
     */
    abstract suspend fun signInWithCredential(credential: AuthCredential?): String?

    /**
     * User id
     */
    abstract val uid: String?
}


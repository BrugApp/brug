package com.github.brugapp.brug.view_model

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.di.sign_in.SignInClient
import com.github.brugapp.brug.di.sign_in.SignInResultHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(private val signInClient: SignInClient,
                                          private val signInResultHandler: SignInResultHandler,
                                          private val lastSignedInAccount: SignInAccount?): ViewModel() {

    // Check for existing Sign In account, if the user is already signed in
    // the SignInAccount will be non-null.
    private var currentUser: User? = createNewBrugUser(lastSignedInAccount)

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun handleSignInResult(it: Intent?) {
        val currentAccount = signInResultHandler.handleSignInResult(it)
        currentUser = createNewBrugUser(currentAccount)
    }

    fun getSignInIntent(): Intent {
        return signInClient.signInIntent
    }

    fun signOut() {
        signInClient.signOut()
        currentUser = null
    }

    // return new Brug User from SignInAccount
    private fun createNewBrugUser(account: SignInAccount?): User? {
        if (account == null) return null
        val firstName = account.firstName
        val lastName = account.lastName
        val email = account.email
        val idToken = account.idToken
        if (firstName == null || lastName == null || email == null || idToken == null) return null
        return User(
            firstName,
            lastName,
            email,
            idToken
        )
    }

}
package com.github.brugapp.brug.view_model

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.data.FirebaseHelper
import com.github.brugapp.brug.data.UserRepo
import com.github.brugapp.brug.di.sign_in.*
import com.github.brugapp.brug.model.MyUser
import com.github.brugapp.brug.model.User
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInClient: SignInClient,
    private val signInResultHandler: SignInResultHandler,
    lastSignedInAccount: SignInAccount?,
    private val auth: AuthDatabase,
    private val credentialGetter: SignInCredentialGetter
) : ViewModel() {

    // Check for existing Sign In account, if the user is already signed in
    // the SignInAccount will be non-null.
    private var currentUser: MyUser? = createNewBrugUser(lastSignedInAccount) //= createNewBrugUser(lastSignedInAccount)

    fun handleSignInResult(it: Intent?): AuthCredential? {
        val currentAccount = signInResultHandler.handleSignInResult(it)
        currentUser = createNewBrugUser(currentAccount)
        return credentialGetter.getCredential(currentAccount?.idToken)
    }

    fun getSignInIntent(): Intent {
        return signInClient.signInIntent
    }

    fun signOut() {
        signInClient.signOut()
        auth.signOut()
        currentUser = null
    }

    // return new Brug User from SignInAccount
    private fun createNewBrugUser(account: SignInAccount?): MyUser? {
        if (account == null || auth.uid == null) return null
        return runBlocking {
            val response = UserRepo.addAuthUserFromAccount(auth.uid!!, account)
            if(response.onSuccess){
                UserRepo.getMinimalUserFromUID(auth.uid!!)
            }
            null
        }

//        return FirebaseHelper.createUserInFirestoreIfAbsent(auth.uid, account)
//        val firstName = account.firstName
//        val lastName = account.lastName
//        val email = account.email
//        val idToken = account.idToken
//        if (firstName == null || lastName == null || email == null || idToken == null) return null
//        return User(
//            firstName,
//            lastName,
//            email,
//            idToken,
//            null
//        )
    }

    fun getAuth(): AuthDatabase {
        return auth
    }

}
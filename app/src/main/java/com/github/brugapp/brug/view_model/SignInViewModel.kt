package com.github.brugapp.brug.view_model

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.*
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInClient: SignInClient,
    private val signInResultHandler: SignInResultHandler,
    lastSignedInAccount: SignInAccount?,
    private val auth: AuthDatabase,
    private val credentialGetter: SignInCredentialGetter,
) : ViewModel() {

    /**
     * Getter for the sign-in intent.
     * @return Intent the sign in intent
     */
    fun getSignInIntent(): Intent {
        return signInClient.signInIntent
    }

    /**
     * Creates a demo user account in the database, if it is not already present.
     *
     * @return Boolean value denoting the state of the sign in procedure (true if successfull, false otherwise)
     */
    suspend fun goToDemoMode(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): Boolean {
        val firebaseAuthResponse = firebaseAuth.signInWithEmailAndPassword(
            "unlost.app@gmail.com",
            "123456").await()

        if(firebaseAuthResponse.user != null){
            return UserRepository.addUserFromAccount(
                firebaseAuthResponse.user!!.uid,
                BrugSignInAccount("Unlost", "DemoUser", "", ""),
                false,
                firestore
            ).onSuccess
        }
        return false
    }

    /**
     * Creates a new user account upon signing into the app, if the account is not already present in the database.
     *
     * @param it the intent holding the result of the sign in procedure
     *
     * @return Boolean value denoting the state of the sign in procedure (true if successful, false otherwise)
     */
    suspend fun createNewBrugAccount(
        it: Intent?,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): Boolean {
        // First we get the account from the account provider (i.e., Google or Unlost)
        val account = signInResultHandler.handleSignInResult(it) ?: return false
        // We also get the credential of the account
        // to add it to the database part handling authentication
        val credential = credentialGetter.getCredential(account.idToken)
        val userID = getAuth().signInWithCredential(credential) ?: return false

        // Finally, add the account if it isn't already in the database
        val result = UserRepository.addUserFromAccount(
            userID,
            account,
            false,
            firestore
        ).onSuccess

        if(result){
            val user = UserRepository.getUserFromUID(userID, firestore, firebaseAuth, firebaseStorage) ?: return false
            BrugDataCache.setUserInCache(user)
        }
        return result
    }

    /**
     * Performs a sign out operation on the currently connected Firebase account.
     */
    suspend fun signOut(firestore: FirebaseFirestore) {
        if(auth.uid != null){
            val deviceToken = FirebaseMessaging.getInstance().token.await()
            UserRepository.deleteDeviceTokenFromUser(
                auth.uid!!,
                deviceToken,
                firestore
            )
            signInClient.signOut()
            auth.signOut()
            resetCachedData()
        }
    }

    private fun resetCachedData() {
        BrugDataCache.resetCachedUser()
        BrugDataCache.resetCachedItems()
        BrugDataCache.resetCachedConversations()
        BrugDataCache.resetCachedMessagesLists()

    }

    fun getAuth(): AuthDatabase {
        return auth
    }
}
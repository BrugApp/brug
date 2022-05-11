package com.github.brugapp.brug.view_model

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.*
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.model.MyUser
import com.github.brugapp.brug.ui.ItemsMenuActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
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
    //private var currentUser: MyUser? = createNewBrugUser(lastSignedInAccount,) //= createNewBrugUser(lastSignedInAccount)
    private var currentUser: MyUser? = null //= createNewBrugUser(lastSignedInAccount)

    fun handleSignInResult(
        it: Intent?,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): AuthCredential? {
        val currentAccount = signInResultHandler.handleSignInResult(it)
        currentUser = createNewBrugUser(currentAccount, firestore, firebaseAuth, firebaseStorage)
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

    fun goToDemoMode(
        activity: AppCompatActivity,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ) {
        liveData(Dispatchers.IO) {
            emit(
                firebaseAuth.signInWithEmailAndPassword(
                    "unlost.app@gmail.com",
                    "brugsdpProject1"
                ).await()
            )
        }.observe(activity) { result ->
            if (result.user != null) {
                if (runBlocking {
                        UserRepository.getMinimalUserFromUID(
                            result.user!!.uid,
                            firestore,
                            firebaseAuth,
                            firebaseStorage
                        )
                    } == null) {
                    runBlocking {
                        UserRepository.addUserFromAccount(
                            firebaseAuth.currentUser!!.uid,
                            BrugSignInAccount(
                                "Unlost",
                                "DemoUser",
                                "",
                                ""
                            ), firestore
                        )
                    }
                }

                activity.startActivity(
                    Intent(
                        activity.applicationContext,
                        ItemsMenuActivity::class.java
                    )
                )
            } else {
                Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    "ERROR: Unable to connect for demo mode", Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }
    }


    // return new Brug User from SignInAccount
    private fun createNewBrugUser(
        account: SignInAccount?,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage
    ): MyUser? {
        if (account == null || auth.uid == null) return null
        return runBlocking {
            val user = UserRepository.getMinimalUserFromUID(
                auth.uid!!,
                firestore,
                firebaseAuth,
                firebaseStorage
            )
            if (user == null) {
                val response = UserRepository.addUserFromAccount(auth.uid!!, account, firestore)
                if (response.onSuccess) {
                    UserRepository.getMinimalUserFromUID(
                        auth.uid!!,
                        firestore,
                        firebaseAuth,
                        firebaseStorage
                    )
                }
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
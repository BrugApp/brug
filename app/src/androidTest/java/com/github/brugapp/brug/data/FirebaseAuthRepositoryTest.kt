package com.github.brugapp.brug.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.di.sign_in.SignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseAuthRepositoryTest {

    private val passwd = "123456"
    private val mAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()

    @After
    fun tearDown() {
        mAuth.signOut()
    }

    @Test
    fun createAuthAccountThrowsErrorOnAlreadyExistingAccount() = runBlocking {
        val dummyAccount = BrugSignInAccount("DUMMYFNAME", "DUMMYLNAME", "", "error@login.com")
        createAccount()

        assertThat(FirebaseAuthRepository.createAuthAccount(dummyAccount, passwd,mAuth, true, firestore).onError, IsNot(IsNull.nullValue()))
    }


    private fun createAccount() {
        val firebaseAuth = FirebaseFakeHelper().providesAuth()
        runBlocking {
            firebaseAuth.createUserWithEmailAndPassword(
                "error@login.com",
                passwd
            ).await()
        }
    }


    @Test
    fun canCreateAccount() {
        val account: SignInAccount = BrugSignInAccount("Test", "User", "", "can@create.com")

        val response: FirebaseResponse
        runBlocking {
            response = FirebaseAuthRepository.createAuthAccount(
                account,
                passwd,
                mAuth,
                true,
                firestore
            )
        }

        assertThat(response.onSuccess, IsEqual(true))

    }
}

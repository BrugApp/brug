package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.data.FirebaseAuthRepository
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseAuthRepositoryTest {

    private val TEST_USER_EMAIL = "dummy@mail.com"
    private val PASSWORD = "123456"

    @Test
    fun createAuthAccountThrowsErrorOnAlreadyExistingAccount() = runBlocking {
        val dummyAccount = BrugSignInAccount("DUMMYFNAME", "DUMMYLNAME", "", TEST_USER_EMAIL)
        createAccout()
        //Why have we to create a account before ?
        assertThat(FirebaseAuthRepository.createAuthAccount(dummyAccount, PASSWORD,FirebaseFakeHelper().providesAuth(), true, FirebaseFakeHelper().providesFirestore()).onError, IsNot(IsNull.nullValue()))
    }

    private fun createAccout() {
        val firebaseAuth = FirebaseFakeHelper().providesAuth()
        runBlocking {
                firebaseAuth.createUserWithEmailAndPassword(
                    TEST_USER_EMAIL,
                    PASSWORD
                ).await()
            }
        }

    //TODO: NEED TO FIGURE OUT HOW TO DELETE AN ENTRY FROM AUTH DATABASE WITHOUT HAVING TO BE ADMIN
//    @Test
//    fun createAuthAccountCorrectlyReturnsOnNonExistingAccount() = runBlocking {
//        val dummyAccount = BrugSignInAccount("DUMMYFNAME", "DUMMYLNAME", "", "dummy@mail.com")
//        assertThat(FirebaseHelper.createAuthAccount(dummyAccount, "DUMMYPASSWD").onSuccess, IsEqual(true))
//    }
}
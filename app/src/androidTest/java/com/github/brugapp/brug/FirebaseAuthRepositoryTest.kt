package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.BrugSignInAccount
import com.github.brugapp.brug.data.FirebaseAuthRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseAuthRepositoryTest {
    val auth = Firebase.auth

    @Test
    fun createAuthAccountThrowsErrorOnAlreadyExistingAccount() = runBlocking {
        val dummyAccount = BrugSignInAccount("DUMMYFNAME", "DUMMYLNAME", "", "dummy@mail.com")
        assertThat(FirebaseAuthRepository.createAuthAccount(dummyAccount, "DUMMYPASSWD").onError, IsNot(IsNull.nullValue()))
    }

    //TODO: NEED TO FIGURE OUT HOW TO DELETE AN ENTRY FROM AUTH DATABASE WITHOUT HAVING TO BE ADMIN
//    @Test
//    fun createAuthAccountCorrectlyReturnsOnNonExistingAccount() = runBlocking {
//        val dummyAccount = BrugSignInAccount("DUMMYFNAME", "DUMMYLNAME", "", "dummy@mail.com")
//        assertThat(FirebaseHelper.createAuthAccount(dummyAccount, "DUMMYPASSWD").onSuccess, IsEqual(true))
//    }
}
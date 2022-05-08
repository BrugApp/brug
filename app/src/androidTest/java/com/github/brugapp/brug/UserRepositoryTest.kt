package com.github.brugapp.brug

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.MyUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test

private const val DUMMY_UID = "AUTHUSERID"
private val DUMMY_ACCOUNT = BrugSignInAccount("Rayan", "Kikou", "", "")

class UserRepositoryTest {

    val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    @Test
    fun getMinimalUserWithWrongUIDReturnsNull() = runBlocking {
        assertThat(UserRepository.getMinimalUserFromUID("WRONGUID",FirebaseFakeHelper().providesFirestore(),FirebaseFakeHelper().providesAuth(),FirebaseFakeHelper().providesStorage()), IsNull.nullValue())
    }

    @Test
    fun getMinimalUserWithGoodUIDReturnsUser() = runBlocking {
        assertThat(UserRepository.getMinimalUserFromUID(DUMMY_UID,FirebaseFakeHelper().providesFirestore(),FirebaseFakeHelper().providesAuth(),FirebaseFakeHelper().providesStorage()), IsNot(IsNull.nullValue()))
    }

    @Test
    fun addAuthUserCorrectlyAddsUser() = runBlocking {
        assertThat(UserRepository.addUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT,FirebaseFakeHelper().providesFirestore()).onSuccess, IsEqual(true))
        val user = UserRepository.getMinimalUserFromUID(DUMMY_UID,FirebaseFakeHelper().providesFirestore(),FirebaseFakeHelper().providesAuth(),FirebaseFakeHelper().providesStorage())
        assertThat(user, IsNot(IsNull.nullValue()))
        assertThat(user, IsEqual(MyUser(DUMMY_UID, DUMMY_ACCOUNT.firstName, DUMMY_ACCOUNT.lastName, null)))
    }

    @Test
    fun updateUserFieldsOfInexistentUserReturnsError() = runBlocking {
        val wrongUser = MyUser("WRONGUID", "BAD", "USER", null)
        assertThat(UserRepository.updateUserFields(wrongUser,FirebaseFakeHelper().providesFirestore()).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateUserCorrectlyUpdatesUserFields() = runBlocking {
        UserRepository.addUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT,FirebaseFakeHelper().providesFirestore())
        val updatedUser = MyUser(DUMMY_UID, "Bryan", "Kikou", null)
        assertThat(UserRepository.updateUserFields(updatedUser,FirebaseFakeHelper().providesFirestore()).onSuccess, IsEqual(true))

        val user = UserRepository.getMinimalUserFromUID(DUMMY_UID,FirebaseFakeHelper().providesFirestore(),FirebaseFakeHelper().providesAuth(),FirebaseFakeHelper().providesStorage())
        assertThat(user, IsNot(IsNull.nullValue()))
        assertThat(user, IsEqual(updatedUser))
    }

    @Test
    fun updateUserIconWithoutAuthReturnsError() = runBlocking {
        // CREATE DRAWABLE
        val drawable = ApplicationProvider.getApplicationContext<Context>().getDrawable(R.drawable.ic_baseline_person_24)
        assertThat(drawable, IsNot(IsNull.nullValue()))

        assertThat(UserRepository.updateUserIcon(DUMMY_UID, drawable!!,FirebaseFakeHelper().providesAuth(),FirebaseFakeHelper().providesStorage(),FirebaseFakeHelper().providesFirestore()).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateUserIconToInexistentUserReturnsError() = runBlocking {
        // CREATE DRAWABLE
        val drawable = ApplicationProvider.getApplicationContext<Context>().getDrawable(R.drawable.ic_baseline_person_24)
        assertThat(drawable, IsNot(IsNull.nullValue()))

        //dummy email and password
        val email = "test@InexistantUser.com"
        val password = "123456"
        //create user
        firebaseAuth.createUserWithEmailAndPassword(email,password)
        // AUTHENTICATE USER TO FIREBASE TO BE ABLE TO USE FIREBASE STORAGE
        val authUser = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()
            .user
        assertThat(firebaseAuth.currentUser, IsNot(IsNull.nullValue()))
        assertThat(firebaseAuth.currentUser!!.uid, IsEqual(authUser!!.uid))

        assertThat(UserRepository.updateUserIcon("WRONGUID", drawable!!,FirebaseFakeHelper().providesAuth(),FirebaseFakeHelper().providesStorage(),FirebaseFakeHelper().providesFirestore()).onError, IsNot(IsNull.nullValue()))
        firebaseAuth.signOut()
    }

    @Test
    fun updateUserIconWithAuthCorrectlyUpdatesIcon() = runBlocking {
        // CREATE DRAWABLE
        val drawable = ApplicationProvider.getApplicationContext<Context>().getDrawable(R.drawable.ic_baseline_person_24)
        assertThat(drawable, IsNot(IsNull.nullValue()))

        //create a new account
        val email = "test@userRepo.com"
        val password = "123456"
        firebaseAuth.createUserWithEmailAndPassword(email,password).await()
        // AUTHENTICATE USER TO FIREBASE TO BE ABLE TO USE FIREBASE STORAGE
        val authUser = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()
            .user
        val uid = authUser!!.uid
        assertThat(firebaseAuth.currentUser, IsNot(IsNull.nullValue()))
        assertThat(firebaseAuth.currentUser!!.uid, IsEqual(uid))
        UserRepository.addUserFromAccount(uid, DUMMY_ACCOUNT,FirebaseFakeHelper().providesFirestore())
        val response = UserRepository.updateUserIcon(uid, drawable!!,FirebaseFakeHelper().providesAuth(),FirebaseFakeHelper().providesStorage(),FirebaseFakeHelper().providesFirestore())
        Log.d("TEST", "updateUserIconWithAuthCorrectlyUpdatesIcon: $response")
        assertThat(response.onError, IsNull.nullValue())

        val updatedUser = UserRepository.getMinimalUserFromUID(uid,FirebaseFakeHelper().providesFirestore(),FirebaseFakeHelper().providesAuth(),FirebaseFakeHelper().providesStorage())
        firebaseAuth.signOut()

        assertThat(updatedUser, IsNot(IsNull.nullValue()))
        assertThat(updatedUser!!.getUserIconPath(), IsNot(IsNull.nullValue()))
//        assertThat(
//            updatedUser.getUserIconPath()!!.intrinsicWidth / updatedUser.getUserIcon()!!.intrinsicHeight,
//            IsEqual(drawable.intrinsicWidth / drawable.intrinsicHeight))
    }

    @Test
    fun resetUserIconOfInexistentUserReturnsError() = runBlocking {
        assertThat(UserRepository.resetUserIcon("WRONGUID",FirebaseFakeHelper().providesFirestore()).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun resetUserIconOfExistingUserReturnsSuccessfully() = runBlocking {
        assertThat(UserRepository.resetUserIcon(DUMMY_UID,FirebaseFakeHelper().providesFirestore()).onSuccess, IsEqual(true))
    }


    @Test
    fun deleteUserReturnsSuccessfully() = runBlocking {
        UserRepository.addUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT,FirebaseFakeHelper().providesFirestore())
//        UserRepo.addAuthUser(DUMMY_USER)
        assertThat(UserRepository.deleteUserFromID(DUMMY_UID, FirebaseFakeHelper().providesFirestore()).onSuccess, IsEqual(true))
        assertThat(UserRepository.getMinimalUserFromUID(DUMMY_UID,FirebaseFakeHelper().providesFirestore(),FirebaseFakeHelper().providesAuth(),FirebaseFakeHelper().providesStorage()), IsNull.nullValue())
    }

    @Test
    fun deleteInexistentUserReturnsError() = runBlocking {
        assertThat(UserRepository.deleteUserFromID("WRONGUID",FirebaseFakeHelper().providesFirestore()).onError, IsNot(IsNull.nullValue()))
    }
}
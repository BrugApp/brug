package com.github.brugapp.brug

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.data.BrugSignInAccount
import com.github.brugapp.brug.model.MyUser
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
    @Test
    fun getMinimalUserWithWrongUIDReturnsNull() = runBlocking {
        assertThat(UserRepository.getMinimalUserFromUID("WRONGUID"), IsNull.nullValue())
    }

    @Test
    fun getMinimalUserWithGoodUIDReturnsUser() = runBlocking {
        assertThat(UserRepository.getMinimalUserFromUID(DUMMY_UID), IsNot(IsNull.nullValue()))
    }

    @Test
    fun addAuthUserCorrectlyAddsUser() = runBlocking {
        assertThat(UserRepository.addUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT).onSuccess, IsEqual(true))
        val user = UserRepository.getMinimalUserFromUID(DUMMY_UID)
        assertThat(user, IsNot(IsNull.nullValue()))
        assertThat(user, IsEqual(MyUser(DUMMY_UID, DUMMY_ACCOUNT.firstName, DUMMY_ACCOUNT.lastName, null)))
    }

    @Test
    fun updateUserFieldsOfInexistentUserReturnsError() = runBlocking {
        val wrongUser = MyUser("WRONGUID", "BAD", "USER", null)
        assertThat(UserRepository.updateUserFields(wrongUser).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateUserCorrectlyUpdatesUserFields() = runBlocking {
        UserRepository.addUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT)
        val updatedUser = MyUser(DUMMY_UID, "Bryan", "Kikou", null)
        assertThat(UserRepository.updateUserFields(updatedUser).onSuccess, IsEqual(true))

        val user = UserRepository.getMinimalUserFromUID(DUMMY_UID)
        assertThat(user, IsNot(IsNull.nullValue()))
        assertThat(user, IsEqual(updatedUser))
    }

    @Test
    fun updateUserIconWithoutAuthReturnsError() = runBlocking {
        // CREATE DRAWABLE
        val drawable = ApplicationProvider.getApplicationContext<Context>().getDrawable(R.drawable.ic_baseline_person_24)
        assertThat(drawable, IsNot(IsNull.nullValue()))

        assertThat(UserRepository.updateUserIcon(DUMMY_UID, drawable!!).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateUserIconToInexistentUserReturnsError() = runBlocking {
        // CREATE DRAWABLE
        val drawable = ApplicationProvider.getApplicationContext<Context>().getDrawable(R.drawable.ic_baseline_person_24)
        assertThat(drawable, IsNot(IsNull.nullValue()))

        // AUTHENTICATE USER TO FIREBASE TO BE ABLE TO USE FIREBASE STORAGE
        val authUser = Firebase.auth
            .signInWithEmailAndPassword("test@unlost.com", "123456")
            .await()
            .user
        assertThat(Firebase.auth.currentUser, IsNot(IsNull.nullValue()))
        assertThat(Firebase.auth.currentUser!!.uid, IsEqual(authUser!!.uid))

        assertThat(UserRepository.updateUserIcon("WRONGUID", drawable!!).onError, IsNot(IsNull.nullValue()))
        Firebase.auth.signOut()
    }

    @Test
    fun updateUserIconWithAuthCorrectlyUpdatesIcon() = runBlocking {
        // CREATE DRAWABLE
        val drawable = ApplicationProvider.getApplicationContext<Context>().getDrawable(R.drawable.ic_baseline_person_24)
        assertThat(drawable, IsNot(IsNull.nullValue()))

        // AUTHENTICATE USER TO FIREBASE TO BE ABLE TO USE FIREBASE STORAGE
        val authUser = Firebase.auth
            .signInWithEmailAndPassword("test@unlost.com", "123456")
            .await()
            .user
        assertThat(Firebase.auth.currentUser, IsNot(IsNull.nullValue()))
        assertThat(Firebase.auth.currentUser!!.uid, IsEqual(authUser!!.uid))

        val response = UserRepository.updateUserIcon(DUMMY_UID, drawable!!)
        assertThat(response.onError, IsNull.nullValue())

        val updatedUser = UserRepository.getMinimalUserFromUID(DUMMY_UID)
        Firebase.auth.signOut()

        assertThat(updatedUser, IsNot(IsNull.nullValue()))
        assertThat(updatedUser!!.getUserIcon(), IsNot(IsNull.nullValue()))
        assertThat(
            updatedUser.getUserIcon()!!.intrinsicWidth / updatedUser.getUserIcon()!!.intrinsicHeight,
            IsEqual(drawable.intrinsicWidth / drawable.intrinsicHeight))
    }

    @Test
    fun resetUserIconOfInexistentUserReturnsError() = runBlocking {
        assertThat(UserRepository.resetUserIcon("WRONGUID").onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun resetUserIconOfExistingUserReturnsSuccessfully() = runBlocking {
        assertThat(UserRepository.resetUserIcon(DUMMY_UID).onSuccess, IsEqual(true))
    }


    @Test
    fun deleteUserReturnsSuccessfully() = runBlocking {
        UserRepository.addUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT)
//        UserRepo.addAuthUser(DUMMY_USER)
        assertThat(UserRepository.deleteUserFromID(DUMMY_UID).onSuccess, IsEqual(true))
        assertThat(UserRepository.getMinimalUserFromUID(DUMMY_UID), IsNull.nullValue())
    }

    @Test
    fun deleteInexistentUserReturnsError() = runBlocking {
        assertThat(UserRepository.deleteUserFromID("WRONGUID").onError, IsNot(IsNull.nullValue()))
    }
}
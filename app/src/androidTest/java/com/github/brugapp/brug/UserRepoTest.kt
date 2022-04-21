package com.github.brugapp.brug

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.brugapp.brug.data.UserRepo
import com.github.brugapp.brug.fake.FakeSignInAccount
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
private val DUMMY_ACCOUNT = FakeSignInAccount("Rayan", "Kikou", "", "")

class UserRepoTest {
    @Test
    fun getMinimalUserWithWrongUIDReturnsNull() = runBlocking {
        assertThat(UserRepo.getMinimalUserFromUID("WRONGUID"), IsNull.nullValue())
    }

    @Test
    fun addAuthUserCorrectlyAddsUser() = runBlocking {
        assertThat(UserRepo.addAuthUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT).onSuccess, IsEqual(true))
        val user = UserRepo.getMinimalUserFromUID(DUMMY_UID)
        assertThat(user, IsNot(IsNull.nullValue()))
        assertThat(user, IsEqual(MyUser(DUMMY_UID, DUMMY_ACCOUNT.firstName, DUMMY_ACCOUNT.lastName, null)))
    }

    @Test
    fun updateUserCorrectlyUpdatesUserFields() = runBlocking {
        UserRepo.addAuthUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT)
        val updatedUser = MyUser(DUMMY_UID, "Bryan", "Kikou", null)
        assertThat(UserRepo.updateUserFields(updatedUser).onSuccess, IsEqual(true))

        val user = UserRepo.getMinimalUserFromUID(DUMMY_UID)
        assertThat(user, IsNot(IsNull.nullValue()))
        assertThat(user, IsEqual(updatedUser))
    }

    @Test
    fun updateUserIconWithAuthCorrectlyUpdatesIcon() = runBlocking {
        // CREATE DRAWABLE
        val drawable = ApplicationProvider.getApplicationContext<Context>().getDrawable(R.drawable.ic_baseline_person_24)
        assertThat(drawable, IsNot(IsNull.nullValue()))

        // AUTHENTICATE USER TO FIREBASE TO BE ABLE TO USE FIREBASE STORAGE
        val authUser = Firebase.auth
            .signInWithEmailAndPassword("unlost.app@gmail.com", "brugsdpProject1")
            .await()
            .user
        assertThat(Firebase.auth.currentUser, IsNot(IsNull.nullValue()))
        assertThat(Firebase.auth.currentUser!!.uid, IsEqual(authUser!!.uid))

        val response = UserRepo.updateUserIcon(DUMMY_UID, drawable!!)
        assertThat(response.onError, IsNull.nullValue())

        val updatedUser = UserRepo.getMinimalUserFromUID(DUMMY_UID)
        Firebase.auth.signOut()

        assertThat(updatedUser, IsNot(IsNull.nullValue()))
        assertThat(updatedUser!!.getUserIcon(), IsNot(IsNull.nullValue()))
        assertThat(
            updatedUser.getUserIcon()!!.intrinsicWidth / updatedUser.getUserIcon()!!.intrinsicHeight,
            IsEqual(drawable.intrinsicWidth / drawable.intrinsicHeight))
    }

    @Test
    fun deleteUserReturnsSuccessfully() = runBlocking {
        UserRepo.addAuthUserFromAccount(DUMMY_UID, DUMMY_ACCOUNT)
//        UserRepo.addAuthUser(DUMMY_USER)
        assertThat(UserRepo.deleteUserFromID(DUMMY_UID).onSuccess, IsEqual(true))
        assertThat(UserRepo.getMinimalUserFromUID(DUMMY_UID), IsNull.nullValue())
    }
}
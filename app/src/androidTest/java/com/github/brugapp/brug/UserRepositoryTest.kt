package com.github.brugapp.brug

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import com.github.brugapp.brug.data.FirebaseResponse
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val DUMMY_UID = "AUTHUSERID"
private const val DEVICE_TOKEN = "siuiwaduaiwniuhaiuJJKJKi"
private val DUMMY_ACCOUNT = BrugSignInAccount("Rayan", "Kikou", "", "")

class UserRepositoryTest {

    @Before
    fun setUp() {
        runBlocking { UserRepository.addUserFromAccount(
            DUMMY_UID,
            DUMMY_ACCOUNT,
            true,
            FirebaseFakeHelper().providesFirestore()
        ) }
    }

    @After
    fun cleanUp(){
        runBlocking {
            UserRepository.deleteDeviceTokenFromUser(
                DUMMY_UID,
                DEVICE_TOKEN,
                FirebaseFakeHelper().providesFirestore()
            )
            UserRepository.deleteUserFromID(
                DUMMY_UID,
                FirebaseFakeHelper().providesFirestore()
            )
        }
    }

    val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    @Test
    fun getMinimalUserWithWrongUIDReturnsNull() = runBlocking {
        assertThat(UserRepository.getUserFromUID(
            "WRONGUID",
            FirebaseFakeHelper().providesFirestore(),
            FirebaseFakeHelper().providesAuth(),
            FirebaseFakeHelper().providesStorage()
        ), IsNull.nullValue())
    }

    @Test
    fun getMinimalUserWithGoodUIDReturnsUser() = runBlocking {
        assertThat(UserRepository.getUserFromUID(
            DUMMY_UID,
            FirebaseFakeHelper().providesFirestore(),
            FirebaseFakeHelper().providesAuth(),
            FirebaseFakeHelper().providesStorage()
        ), IsNot(IsNull.nullValue()))
    }

    @Test
    fun addAuthUserCorrectlyAddsUser() = runBlocking {
        assertThat(UserRepository.addUserFromAccount(
            DUMMY_UID,
            DUMMY_ACCOUNT,
            true,
            FirebaseFakeHelper().providesFirestore()
        ).onSuccess, IsEqual(true))
        val user = UserRepository.getUserFromUID(
            DUMMY_UID,
            FirebaseFakeHelper().providesFirestore(),
            FirebaseFakeHelper().providesAuth(),
            FirebaseFakeHelper().providesStorage()
        )
        assertThat(user, IsNot(IsNull.nullValue()))
        assertThat(user, IsEqual(User(DUMMY_UID, DUMMY_ACCOUNT.firstName, DUMMY_ACCOUNT.lastName, null, mutableListOf())))
    }

    @Test
    fun updateUserFieldsOfInexistentUserReturnsError() = runBlocking {
        val wrongUser = User("WRONGUID", "BAD", "USER", null, mutableListOf())
        assertThat(UserRepository.updateUserFields(
            wrongUser,
            FirebaseFakeHelper().providesFirestore()
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun updateUserCorrectlyUpdatesUserFields() = runBlocking {
        UserRepository.addUserFromAccount(
            DUMMY_UID,
            DUMMY_ACCOUNT,
            true,
            FirebaseFakeHelper().providesFirestore()
        )
        val updatedUser = User(DUMMY_UID, "Bryan", "Kikou", null, mutableListOf())
        assertThat(UserRepository.updateUserFields(
            updatedUser,
            FirebaseFakeHelper().providesFirestore()
        ).onSuccess, IsEqual(true))

        val user = UserRepository.getUserFromUID(
            DUMMY_UID,
            FirebaseFakeHelper().providesFirestore(),
            FirebaseFakeHelper().providesAuth(),
            FirebaseFakeHelper().providesStorage()
        )
        assertThat(user, IsNot(IsNull.nullValue()))
        assertThat(user, IsEqual(updatedUser))
    }

    @Test
    fun addDeviceTokenToInexistentUserReturnsError() = runBlocking {
        val wrongUser = User("WRONGUID", "BAD", "USER", null, mutableListOf())
        val observableResponse = MutableLiveData<FirebaseResponse>()
        UserRepository.addNewDeviceTokenToUser(wrongUser.uid, DEVICE_TOKEN, observableResponse, FirebaseFakeHelper().providesFirestore())
        observableResponse.observe(TestLifecycleOwner()){ response ->
            assertThat(response.onError, IsNot(IsNull.nullValue()))
        }
    }

    @Test
    fun addDeviceTokenToExistentUserReturnsSuccessfully() {
        runBlocking {
            UserRepository.addUserFromAccount(
                DUMMY_UID,
                DUMMY_ACCOUNT,
                true,
                FirebaseFakeHelper().providesFirestore()
            )
        }

        val observableResponse = MutableLiveData<FirebaseResponse>()
        UserRepository.addNewDeviceTokenToUser(
            DUMMY_UID,
            DEVICE_TOKEN,
            observableResponse,
            FirebaseFakeHelper().providesFirestore()
        )

        observableResponse.observe(TestLifecycleOwner()){ response ->
            assertThat(response.onSuccess, IsEqual(true))
            val updatedUser = User(DUMMY_UID, "Rayan", "Kikou", null, mutableListOf(DEVICE_TOKEN))
            runBlocking {
                val user = UserRepository.getUserFromUID(
                    DUMMY_UID,
                    FirebaseFakeHelper().providesFirestore(),
                    FirebaseFakeHelper().providesAuth(),
                    FirebaseFakeHelper().providesStorage()
                )
                assertThat(user, IsNot(IsNull.nullValue()))
                assertThat(user, IsEqual(updatedUser))
            }
        }
    }

    @Test
    fun updateUserIconWithoutAuthReturnsError() = runBlocking {
        // CREATE DRAWABLE
        val drawable = ApplicationProvider.getApplicationContext<Context>().getDrawable(R.drawable.ic_baseline_person_24)
        assertThat(drawable, IsNot(IsNull.nullValue()))

        assertThat(UserRepository.updateUserIcon(
            DUMMY_UID,
            drawable!!,
            FirebaseFakeHelper().providesAuth(),
            FirebaseFakeHelper().providesStorage(),
            FirebaseFakeHelper().providesFirestore()
        ).onError, IsNot(IsNull.nullValue()))
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
        firebaseAuth.createUserWithEmailAndPassword(email,password).await()
        // AUTHENTICATE USER TO FIREBASE TO BE ABLE TO USE FIREBASE STORAGE
        val authUser = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()
            .user
        assertThat(firebaseAuth.currentUser, IsNot(IsNull.nullValue()))
        assertThat(firebaseAuth.currentUser!!.uid, IsEqual(authUser!!.uid))

        assertThat(UserRepository.updateUserIcon(
            "WRONGUID",
            drawable!!,
            FirebaseFakeHelper().providesAuth(),
            FirebaseFakeHelper().providesStorage(),
            FirebaseFakeHelper().providesFirestore()
        ).onError, IsNot(IsNull.nullValue()))
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
        UserRepository.addUserFromAccount(
            uid,
            DUMMY_ACCOUNT,
            true,
            FirebaseFakeHelper().providesFirestore()
        )
        val response = UserRepository.updateUserIcon(
            uid,
            drawable!!,
            FirebaseFakeHelper().providesAuth(),
            FirebaseFakeHelper().providesStorage(),
            FirebaseFakeHelper().providesFirestore()
        )
        Log.d("TEST", "updateUserIconWithAuthCorrectlyUpdatesIcon: $response")
        assertThat(response.onError, IsNull.nullValue())

        val updatedUser = UserRepository.getUserFromUID(
            uid,
            FirebaseFakeHelper().providesFirestore(),
            FirebaseFakeHelper().providesAuth(),
            FirebaseFakeHelper().providesStorage()
        )
        firebaseAuth.signOut()

        assertThat(updatedUser, IsNot(IsNull.nullValue()))
        assertThat(updatedUser!!.getUserIconPath(), IsNot(IsNull.nullValue()))
//        assertThat(
//            updatedUser.getUserIconPath()!!.intrinsicWidth / updatedUser.getUserIcon()!!.intrinsicHeight,
//            IsEqual(drawable.intrinsicWidth / drawable.intrinsicHeight))
    }

    @Test
    fun resetUserIconOfInexistentUserReturnsError() = runBlocking {
        assertThat(UserRepository.resetUserIcon(
            "WRONGUID",
            FirebaseFakeHelper().providesFirestore()
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun resetUserIconOfExistingUserReturnsSuccessfully() = runBlocking {
        assertThat(UserRepository.resetUserIcon(
            DUMMY_UID,
            FirebaseFakeHelper().providesFirestore()
        ).onSuccess, IsEqual(true))
    }


    @Test
    fun deleteUserReturnsSuccessfully() = runBlocking {
        UserRepository.addUserFromAccount(
            DUMMY_UID,
            DUMMY_ACCOUNT,
            true,
            FirebaseFakeHelper().providesFirestore()
        )
//        UserRepo.addAuthUser(DUMMY_USER)
        assertThat(UserRepository.deleteUserFromID(
            DUMMY_UID,
            FirebaseFakeHelper().providesFirestore()
        ).onSuccess, IsEqual(true))
        assertThat(UserRepository.getUserFromUID(
            DUMMY_UID,
            FirebaseFakeHelper().providesFirestore(),
            FirebaseFakeHelper().providesAuth(),
            FirebaseFakeHelper().providesStorage()
        ), IsNull.nullValue())
    }

    @Test
    fun deleteInexistentUserReturnsError() = runBlocking {
        assertThat(UserRepository.deleteUserFromID(
            "WRONGUID",
            FirebaseFakeHelper().providesFirestore()
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteDeviceTokenFromExistingUserReturnsSuccessfully() {
        val userWithoutToken =
            User(DUMMY_UID, DUMMY_ACCOUNT.firstName, DUMMY_ACCOUNT.lastName, null, mutableListOf())
        val userWithToken = User(
            DUMMY_UID,
            DUMMY_ACCOUNT.firstName,
            DUMMY_ACCOUNT.lastName,
            null,
            mutableListOf(DEVICE_TOKEN)
        )

        runBlocking {
            UserRepository.addUserFromAccount(
                DUMMY_UID,
                DUMMY_ACCOUNT,
                true,
                FirebaseFakeHelper().providesFirestore()
            )
        }

        val observableResponse = MutableLiveData<FirebaseResponse>()
        UserRepository.addNewDeviceTokenToUser(
            DUMMY_UID,
            DEVICE_TOKEN,
            observableResponse,
            FirebaseFakeHelper().providesFirestore()
        )

        observableResponse.observe(TestLifecycleOwner()){ response ->
            assertThat(response.onSuccess, IsEqual(true))
            runBlocking {
                assertThat(UserRepository.getUserFromUID(
                    DUMMY_UID,
                    FirebaseFakeHelper().providesFirestore(),
                    FirebaseFakeHelper().providesAuth(),
                    FirebaseFakeHelper().providesStorage()
                ), IsEqual(userWithToken))
    //        UserRepo.addAuthUser(DUMMY_USER)
                assertThat(UserRepository.deleteDeviceTokenFromUser(
                    DUMMY_UID,
                    DEVICE_TOKEN,
                    FirebaseFakeHelper().providesFirestore()
                ).onSuccess, IsEqual(true))
                assertThat(UserRepository.getUserFromUID(
                    DUMMY_UID,
                    FirebaseFakeHelper().providesFirestore(),
                    FirebaseFakeHelper().providesAuth(),
                    FirebaseFakeHelper().providesStorage()
                ), IsEqual(userWithoutToken))
            }
        }
    }

    @Test
    fun deleteDeviceTokenFromInexistentUserReturnsError() = runBlocking {
        assertThat(UserRepository.deleteDeviceTokenFromUser(
            "WRONGUID",
            DEVICE_TOKEN,
            FirebaseFakeHelper().providesFirestore()
        ).onError, IsNot(IsNull.nullValue()))
    }
}
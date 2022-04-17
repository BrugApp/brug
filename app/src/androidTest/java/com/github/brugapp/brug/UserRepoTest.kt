package com.github.brugapp.brug

import android.graphics.drawable.Drawable
import android.util.Log
import com.github.brugapp.brug.data.UserRepo
import com.github.brugapp.brug.model.MyUser
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test

private const val AUTH_UID = "AUTHUSERID"
private val DUMMY_USER = MyUser(AUTH_UID, "Rayan", "Kikou", null)

class UserRepoTest {
    @Test
    fun getMinimalUserWithWrongUIDReturnsError() = runBlocking {
        assertThat(UserRepo.getMinimalUserFromUID("WRONGUID"), IsNull.nullValue())
    }

    @Test
    fun getFullUserWithWrongUIDReturnsNull() = runBlocking {
        assertThat(UserRepo.getFullUserFromUID("WRONGUID"), IsNull.nullValue())
    }

    @Test
    fun addAuthUserReturnsSuccessfully() = runBlocking {
        assertThat(UserRepo.addAuthUser(DUMMY_USER).onSuccess, IsEqual(true))
    }

    @Test
    fun updateUserReturnsSuccessfully() = runBlocking {
        UserRepo.addAuthUser(DUMMY_USER)
        val updatedUser = MyUser(DUMMY_USER.uid, "Bryan", "Kikou", null)
        assertThat(UserRepo.updateUserFields(updatedUser).onSuccess, IsEqual(true))
    }

    @Test
    fun updateUserIconWithoutAuthThrowsError() = runBlocking {
        //TODO: SOLVE DRAWABLE GENERATION ERROR
        val newIcon = Drawable.createFromPath("/res/mipmap-hdpi/ic_launcher/ic_launcher.webp")
        if(newIcon == null){
            Log.e("FATAL ERROR", "NEWICON IS NULL")
        }
        assertThat(UserRepo.updateUserIcon(DUMMY_USER.uid, newIcon!!).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteUserReturnsSuccessfully() = runBlocking {
        UserRepo.addAuthUser(DUMMY_USER)
        assertThat(UserRepo.deleteUserFromID(DUMMY_USER.uid).onSuccess, IsEqual(true))
    }
}
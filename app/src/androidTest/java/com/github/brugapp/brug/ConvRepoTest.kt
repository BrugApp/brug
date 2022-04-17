package com.github.brugapp.brug

import android.util.Log
import com.github.brugapp.brug.data.ConvRepo
import com.github.brugapp.brug.data.UserRepo
import com.github.brugapp.brug.model.MyUser
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.Test

private val USER1 = MyUser("USER1", "Rayan", "Kikou", null)
private val USER2 = MyUser("USER2", "Hamza", "Hassoune", null)
private const val DUMMY_ITEM_NAME = "Airpods"

class ConvRepoTest {
    //NEEDED SINCE @Before FUNCTIONS NEED TO BE VOID
    private fun addTestUsers() = runBlocking{
        UserRepo.addAuthUser(USER1)
        UserRepo.addAuthUser(USER2)
    }

    @Before
    fun setUp() {
        addTestUsers()
    }


    @Test
    fun addNewConvCorrectlyReturns() = runBlocking {
        assertThat(ConvRepo.addNewConversation(USER1.uid, USER2.uid, DUMMY_ITEM_NAME).onSuccess, IsEqual(true))
    }

    @Test
    fun getWrongUserConvReturnsNull() = runBlocking {
        assertThat(ConvRepo.getUserConvFromUID("WRONGCONVID"), IsNull.nullValue())
    }

    @Test
    fun getValidConvCorrectlyReturns() = runBlocking {
        assertThat(ConvRepo.getUserConvFromUID(USER1.uid), IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteNonexistentConvReturnsError() = runBlocking {
        assertThat(ConvRepo.deleteConversationFromID("WRONGCONVID", USER1.uid).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteConvNotBelongingToUserReturnsError() = runBlocking {
        assertThat(ConvRepo.deleteConversationFromID("${USER1.uid}${USER2.uid}", "WRONGUID").onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteValidConvReturnsSuccessfully() = runBlocking {
        ConvRepo.addNewConversation(USER1.uid, USER2.uid, DUMMY_ITEM_NAME)
        assertThat(ConvRepo.deleteConversationFromID("${USER1.uid}${USER2.uid}", USER1.uid).onSuccess, IsEqual(true))
    }
}
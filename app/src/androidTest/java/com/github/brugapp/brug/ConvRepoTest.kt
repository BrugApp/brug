package com.github.brugapp.brug

import com.github.brugapp.brug.data.ConvRepo
import com.github.brugapp.brug.data.UserRepo
import com.github.brugapp.brug.model.Conversation
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
private val USERWITHWRONGCONV = MyUser("USERWITHWRONGCONV", "", "", null)
private const val DUMMY_ITEM_NAME = "Airpods"

class ConvRepoTest {
    //NEEDED SINCE @Before FUNCTIONS NEED TO BE VOID
    private fun addTestUsers() = runBlocking{
        UserRepo.addAuthUser(USER1)
        UserRepo.addAuthUser(USER2)
        UserRepo.addAuthUser(USERWITHWRONGCONV)
    }

    @Before
    fun setUp() {
        addTestUsers()
    }


    @Test
    fun addNewConvCorrectlyReturns() = runBlocking {
        assertThat(ConvRepo.addNewConversation(USER1.uid, USER2.uid, DUMMY_ITEM_NAME).onSuccess, IsEqual(true))
        val conversation = Conversation("${USER1.uid}${USER2.uid}", USER2, DUMMY_ITEM_NAME, mutableListOf())
        val convList = ConvRepo.getUserConvFromUID(USER1.uid)
        assertThat(convList.isNullOrEmpty(), IsEqual(false))
        assertThat(convList!!.contains(conversation), IsEqual(true))
    }

    @Test
    fun getConvsFromNonexistentUserReturnsNull() = runBlocking {
        assertThat(ConvRepo.getUserConvFromUID("WRONGCONVID"), IsNull.nullValue())
    }

    @Test
    fun getBadlyFormattedConvsReturnsEmptyList() = runBlocking {
        assertThat(ConvRepo.getUserConvFromUID(USERWITHWRONGCONV.uid), IsEqual(listOf()))
    }

    @Test
    fun getConvsFromValidUserCorrectlyReturns() = runBlocking {
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
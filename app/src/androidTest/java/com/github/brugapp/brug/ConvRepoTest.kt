package com.github.brugapp.brug

import com.github.brugapp.brug.data.ConvRepo
import com.github.brugapp.brug.data.UserRepo
import com.github.brugapp.brug.fake.FakeSignInAccount
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.MyUser
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.Test


private const val USER_ID1 = "USER1"
private const val USER_ID2 = "USER2"
private const val USERWRONGCONV_ID = "USERWITHWRONGCONV"
private val ACCOUNT1 = FakeSignInAccount("Rayan", "Kikou", "", "")
private val ACCOUNT2 = FakeSignInAccount("Hamza", "Hassoune", "", "")
private val ACCOUNTWRONGCONV = FakeSignInAccount("", "", "", "")

private val USER2 = MyUser(USER_ID2, ACCOUNT2.firstName, ACCOUNT2.lastName, null)
private const val DUMMY_ITEM_NAME = "Airpods"

class ConvRepoTest {
    //NEEDED SINCE @Before FUNCTIONS NEED TO BE VOID
    private fun addTestUsers() = runBlocking{
        UserRepo.addAuthUserFromAccount(USER_ID1, ACCOUNT1)
        UserRepo.addAuthUserFromAccount(USER_ID2, ACCOUNT2)
        UserRepo.addAuthUserFromAccount(USERWRONGCONV_ID, ACCOUNTWRONGCONV)

//        UserRepo.addAuthUser(USER2)
//        UserRepo.addAuthUser(USERWITHWRONGCONV)
    }

    @Before
    fun setUp() {
        addTestUsers()
    }


    @Test
    fun addNewConvCorrectlyReturns() = runBlocking {
        assertThat(ConvRepo.addNewConversation(USER_ID1, USER_ID2, DUMMY_ITEM_NAME).onSuccess, IsEqual(true))
        val conversation = Conversation("${USER_ID1}${USER_ID2}", USER2, DUMMY_ITEM_NAME, mutableListOf())
        val convList = ConvRepo.getUserConvFromUID(USER_ID1)
        assertThat(convList.isNullOrEmpty(), IsEqual(false))
        assertThat(convList!!.contains(conversation), IsEqual(true))
    }

    @Test
    fun getConvsFromNonexistentUserReturnsNull() = runBlocking {
        assertThat(ConvRepo.getUserConvFromUID("WRONGCONVID"), IsNull.nullValue())
    }

    @Test
    fun getBadlyFormattedConvsReturnsEmptyList() = runBlocking {
        assertThat(ConvRepo.getUserConvFromUID(USERWRONGCONV_ID), IsEqual(listOf()))
    }

    @Test
    fun getConvsFromValidUserCorrectlyReturns() = runBlocking {
        assertThat(ConvRepo.getUserConvFromUID(USER_ID1), IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteNonexistentConvReturnsError() = runBlocking {
        assertThat(ConvRepo.deleteConversationFromID("WRONGCONVID", USER_ID1).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteConvNotBelongingToUserReturnsError() = runBlocking {
        assertThat(ConvRepo.deleteConversationFromID("${USER_ID1}${USER_ID2}", "WRONGUID").onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteValidConvReturnsSuccessfully() = runBlocking {
        ConvRepo.addNewConversation(USER_ID1, USER_ID2, DUMMY_ITEM_NAME)
        assertThat(ConvRepo.deleteConversationFromID("${USER_ID1}${USER_ID2}", USER_ID1).onSuccess, IsEqual(true))
    }
}
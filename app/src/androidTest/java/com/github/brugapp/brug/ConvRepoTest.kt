package com.github.brugapp.brug

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import com.github.brugapp.brug.data.ConvRepo
import com.github.brugapp.brug.data.MessageRepo
import com.github.brugapp.brug.data.UserRepo
import com.github.brugapp.brug.fake.FakeSignInAccount
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.MyUser
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.services.DateService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime


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
    fun addConvToInexistentUsersReturnsError() = runBlocking {
        assertThat(ConvRepo.addNewConversation("WRONGUID", USER_ID2, DUMMY_ITEM_NAME).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun addNewConvCorrectlyReturns() = runBlocking {
        assertThat(ConvRepo.addNewConversation(USER_ID1, USER_ID2, DUMMY_ITEM_NAME).onSuccess, IsEqual(true))
        val conversation = Conversation("${USER_ID1}${USER_ID2}", USER2, DUMMY_ITEM_NAME, mutableListOf())
        val convList = ConvRepo.getUserConvFromUID(USER_ID1)
        assertThat(convList.isNullOrEmpty(), IsEqual(false))
        val conv = convList!!.last()
        assertThat(conv.convId, IsEqual(conversation.convId))
        assertThat(conv.lostItemName, IsEqual(conversation.lostItemName))
        assertThat(conv.userFields, IsEqual(conversation.userFields))
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
    fun getConvsFromValidUserCorrectlyReturnsSuccessfully() = runBlocking {
        assertThat(ConvRepo.getUserConvFromUID(USER_ID1), IsNot(IsNull.nullValue()))
    }

    @Test
    fun getConvWithAttachmentReturnsSuccessfully() = runBlocking {
        // CREATE DRAWABLE
        val drawable = ApplicationProvider.getApplicationContext<Context>().getDrawable(R.mipmap.ic_launcher)
        assertThat(drawable, IsNot(IsNull.nullValue()))

        // AUTHENTICATE USER TO FIREBASE TO BE ABLE TO USE FIREBASE STORAGE
        val authUser = Firebase.auth
            .signInWithEmailAndPassword("test@unlost.com", "123456")
            .await()
            .user
        assertThat(Firebase.auth.currentUser, IsNot(IsNull.nullValue()))
        assertThat(Firebase.auth.currentUser!!.uid, IsEqual(authUser!!.uid))

        val file = File.createTempFile("tempIMG", ".jpg")
        val fos = FileOutputStream(file)

        val bitmap = drawable!!.toBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)

        val picMessage = PicMessage(
            "Me",
            DateService.fromLocalDateTime(LocalDateTime.now()),
            "TestPicMessage",
            Uri.fromFile(file).toString()
        )
        fos.close()

        assertThat(MessageRepo.addMessageToConv(picMessage, USER_ID1,"${USER_ID1}${USER_ID2}").onSuccess, IsEqual(true))

        assertThat(ConvRepo.getUserConvFromUID(USER_ID1), IsNot(IsNull.nullValue()))
        Firebase.auth.signOut()
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

    @Test
    fun deleteAllConvsFromInexistentUserReturnsError() = runBlocking {
        assertThat(ConvRepo.deleteAllUserConversations("WRONGUID").onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteAllConvsFromValidUserReturnsSuccessfully() = runBlocking {
        assertThat(ConvRepo.deleteAllUserConversations(USER_ID1).onSuccess, IsEqual(true))
    }
}
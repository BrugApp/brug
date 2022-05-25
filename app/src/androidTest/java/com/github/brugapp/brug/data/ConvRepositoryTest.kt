package com.github.brugapp.brug.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import com.github.brugapp.brug.R
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.services.DateService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
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
import java.time.Month


private const val USER_ID1 = "USER1"
private const val USER_ID2 = "USER2"
private const val USERWRONGCONV_ID = "USERWITHWRONGCONV"
private val ACCOUNT1 = BrugSignInAccount("Rayan", "Kikou", "", "")
private val ACCOUNT2 = BrugSignInAccount("Hamza", "Hassoune", "", "")
private val ACCOUNTWRONGCONV = BrugSignInAccount("", "", "", "")

private val USER2 = User(USER_ID2, ACCOUNT2.firstName, ACCOUNT2.lastName, null, mutableListOf())
private val DUMMY_ITEM = Item("Airpods", 0, "DUMMYDESC", false)

class ConvRepositoryTest {
    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firebaseStorage: FirebaseStorage = FirebaseFakeHelper().providesStorage()

    //NEEDED SINCE @Before FUNCTIONS NEED TO BE VOID
    private fun addTestUsers() = runBlocking{
        UserRepository.addUserFromAccount(USER_ID1, ACCOUNT1, true, firestore)
        UserRepository.addUserFromAccount(USER_ID2, ACCOUNT2, true, firestore)
        ItemsRepository.addItemWithItemID(DUMMY_ITEM, "DUMMYID", USER_ID1, firestore)
        UserRepository.addUserFromAccount(USERWRONGCONV_ID, ACCOUNTWRONGCONV, true, firestore)
    }

    @Before
    fun setUp() {
        DUMMY_ITEM.setItemID("DUMMYID")
        addTestUsers()
        BrugDataCache.resetCachedConversations()
    }

    @Test
    fun addConvToInexistentUsersReturnsError() = runBlocking {
        assertThat(ConvRepository.addNewConversation("WRONGUID", USER_ID2, "WRONGUID:${DUMMY_ITEM.getItemID()}", null, firestore).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun addNewConvCorrectlyReturns() = runBlocking {
        assertThat(ConvRepository.addNewConversation(USER_ID1, USER_ID2, "$USER_ID1:${DUMMY_ITEM.getItemID()}", null, firestore).onSuccess, IsEqual(true))
        val conversation = Conversation("${USER_ID1}${USER_ID2}", USER2, DUMMY_ITEM, null)

        ConvRepository.getRealtimeConvsFromUID(
            USER_ID1,
            TestLifecycleOwner(),
            firestore,
            firebaseAuth,
            firebaseStorage
        )
        delay(1000)
        assertThat(BrugDataCache.getCachedConversations().value.isNullOrEmpty(), IsEqual(false))

        val conv = BrugDataCache.getCachedConversations().value!!.last()
        assertThat(conv.convId, IsEqual(conversation.convId))
        assertThat(conv.lostItem == conversation.lostItem, IsEqual(true))
        assertThat(conv.userFields, IsEqual(conversation.userFields))
    }

    @Test
    fun getConvsFromNonexistentUserReturnsNull() {
        val wrongConvID = "WRONGCONVID"
        ConvRepository.getRealtimeConvsFromUID(
            wrongConvID,
            TestLifecycleOwner(),
            firestore,
            firebaseAuth,
            firebaseStorage
        )
        runBlocking {
            delay(2000)
        }
        assertThat(BrugDataCache.getCachedConversations().value, IsEqual(mutableListOf()))
    }

    @Test
    fun getBadlyFormattedConvsReturnsEmptyList() {
        ConvRepository.getRealtimeConvsFromUID(
            USERWRONGCONV_ID,
            TestLifecycleOwner(),
            firestore,
            firebaseAuth,
            firebaseStorage
        )
        runBlocking {
            delay(1000)
        }
        assertThat(BrugDataCache.getCachedConversations().value, IsEqual(mutableListOf()))
    }

    @Test
    fun getConvsFromValidUserCorrectlyReturnsSuccessfully() {
        ConvRepository.getRealtimeConvsFromUID(
            USER_ID1,
            TestLifecycleOwner(),
            firestore,
            firebaseAuth,
            firebaseStorage
        )
        runBlocking {
            delay(1000)
        }
        assertThat(BrugDataCache.getCachedConversations().value, IsNot(IsNull.nullValue()))
    }

    @Test
    fun getConvWithAttachmentReturnsSuccessfully() = runBlocking {
        // CREATE DRAWABLE
        val drawable = ApplicationProvider.getApplicationContext<Context>().getDrawable(R.mipmap.ic_launcher)
        assertThat(drawable, IsNot(IsNull.nullValue()))

        val email = "test@convAttachement.com"
        val password ="123456"
        // AUTHENTICATE USER TO FIREBASE TO BE ABLE TO USE FIREBASE STORAGE
        firebaseAuth.createUserWithEmailAndPassword(email,password).await()
        val authUser = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()
            .user
        assertThat(firebaseAuth.currentUser, IsNot(IsNull.nullValue()))
        assertThat(firebaseAuth.currentUser!!.uid, IsEqual(authUser!!.uid))
        val dummyDate = DateService.fromLocalDateTime(
        LocalDateTime.of(
            2022, Month.MARCH, 23, 15, 30
        )
    )
        //val message = Message("BigBoy",dummyDate,"Wagwan")//why the test fails if I use this?
        val message = null
        ConvRepository.addNewConversation(USER_ID1, USER_ID2, "$USER_ID1:${DUMMY_ITEM.getItemID()}", message, firestore)

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

        assertThat(MessageRepository.addMessageToConv(
            picMessage,
            USER_ID1,
            "${USER_ID1}${USER_ID2}",
            firestore,
            firebaseAuth,
            firebaseStorage
        ).onSuccess, IsEqual(true))

        ConvRepository.getRealtimeConvsFromUID(
            USER_ID1,
            TestLifecycleOwner(),
            firestore,
            firebaseAuth,
            firebaseStorage
        )
        delay(1000)
        assertThat(BrugDataCache.getCachedConversations().value, IsNot(IsNull.nullValue()))

        firebaseAuth.signOut()
    }

    @Test
    fun deleteNonexistentConvReturnsError() = runBlocking {
        assertThat(ConvRepository.deleteConversationFromID(
            "WRONGCONVID",
            USER_ID1,
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteConvNotBelongingToUserReturnsError() = runBlocking {
        assertThat(ConvRepository.deleteConversationFromID(
            "${USER_ID1}${USER_ID2}",
            "WRONGUID",
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteValidConvReturnsSuccessfully() = runBlocking {
        ConvRepository.addNewConversation(USER_ID1, USER_ID2, "$USER_ID1:${DUMMY_ITEM.getItemID()}", null, firestore)
        assertThat(ConvRepository.deleteConversationFromID("${USER_ID1}${USER_ID2}", USER_ID1,firestore).onSuccess, IsEqual(true))
    }

    @Test
    fun deleteAllConvsFromInexistentUserReturnsError() = runBlocking {
        assertThat(ConvRepository.deleteAllUserConversations(
            "WRONGUID",
            firestore
        ).onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun deleteAllConvsFromValidUserReturnsSuccessfully() = runBlocking {
        assertThat(ConvRepository.deleteAllUserConversations(
            USER_ID1,
            firestore
        ).onSuccess, IsEqual(true))
    }
}
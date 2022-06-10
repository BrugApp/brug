package com.github.brugapp.brug

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import com.github.brugapp.brug.data.*
import com.github.brugapp.brug.di.sign_in.brug_account.BrugSignInAccount
import com.github.brugapp.brug.fake.FirebaseFakeHelper
import com.github.brugapp.brug.model.Item
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

private const val USER_ID1 = "USER1"
private const val USER_ID2 = "USER2"
private val ACCOUNT1 = BrugSignInAccount("Rayan", "Kikou", "", "")
private val ACCOUNT2 = BrugSignInAccount("Hamza", "Hassoune", "", "")

private val USER2 = User(USER_ID2, ACCOUNT2.firstName, ACCOUNT2.lastName, null, mutableListOf())
private const val DUMMY_ITEM_ID = "DUMMYITEMID"
private val DUMMY_ITEM = Item("AirPods Pro Max", 0, "DUMMYDESC", false)

private val TEXTMSG = TextMessage("Me",
    DateService.fromLocalDateTime(LocalDateTime.now()),
    "TextMessage")

private val LOCATIONMSG = LocationMessage(
    USER2.getFullName(),
    DateService.fromLocalDateTime(LocalDateTime.now()),
    "LocationMessage",
    LocationService.fromGeoPoint(GeoPoint(24.5, 10.8)))


private const val userEmail = "test@Message.com"
private const val passwd = "123456"


class MessageRepositoryTest {
    private val firestore: FirebaseFirestore = FirebaseFakeHelper().providesFirestore()
    private val firebaseAuth: FirebaseAuth = FirebaseFakeHelper().providesAuth()
    private val firebaseStorage: FirebaseStorage = FirebaseFakeHelper().providesStorage()

    private fun addUsersAndConv() = runBlocking {
        UserRepository.addUserFromAccount(USER_ID1, ACCOUNT1, true, firestore)
        UserRepository.addUserFromAccount(USER_ID2, ACCOUNT2, true, firestore)
        DUMMY_ITEM.setItemID(DUMMY_ITEM_ID)
        ItemsRepository.addItemWithItemID(DUMMY_ITEM, DUMMY_ITEM_ID, USER_ID1, firestore)
        ConvRepository.addNewConversation(USER_ID1, USER_ID2, "$USER_ID1:${DUMMY_ITEM.getItemID()}", null, firestore)
    }

    @Before
    fun setUp(){
        addUsersAndConv()
    }

    @After
    fun cleanUp(){
        BrugDataCache.resetCachedMessagesLists()
        if(firebaseAuth.currentUser != null){
            firebaseAuth.signOut()
        }
    }

    @Test
    fun addMessageToWrongConvReturnsError() = runBlocking {
        val response = MessageRepository.addMessageToConv(
            TEXTMSG,
            false,
            USER_ID1,
            "WRONGCONVID",
            firestore,
            firebaseAuth,
            firebaseStorage
        )
        assertThat(response.onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun addTextMessageCorrectlyAddsNewTextMessage() {
        BrugDataCache.initMessageListInCache("${USER_ID1}${USER_ID2}")

        val response = runBlocking {
             MessageRepository.addMessageToConv(
                 TEXTMSG,
                 true,
                 USER_ID1,
                 "${USER_ID1}${USER_ID2}",
                 firestore,
                 firebaseAuth,
                 firebaseStorage
             )
        }
        assertThat(response.onSuccess, IsEqual(true))

        MessageRepository.getRealtimeMessages(
            "${USER_ID1}${USER_ID2}",
            USER2.getFullName(),
            USER_ID1,
            TestLifecycleOwner(),
            null,
            firestore,
            firebaseAuth,
            firebaseStorage
        )

        runBlocking {
            delay(1000)
        }

        assertThat(BrugDataCache.getCachedMessageList("${USER_ID1}${USER_ID2}").value.isNullOrEmpty(), IsEqual(false))
        assertThat(BrugDataCache.getCachedMessageList("${USER_ID1}${USER_ID2}").value!!.contains(TEXTMSG), IsEqual(true))
    }

    @Test
    fun addLocationMessageCorrectlyAddsNewLocationMessage() = runBlocking {
        BrugDataCache.initMessageListInCache("${USER_ID1}${USER_ID2}")

        val response = MessageRepository.addMessageToConv(
            LOCATIONMSG,
            true,
            USER_ID2,
            "${USER_ID1}${USER_ID2}",
            firestore,
            firebaseAuth,
            firebaseStorage
        )
        assertThat(response.onSuccess, IsEqual(true))


        MessageRepository.getRealtimeMessages(
            "${USER_ID1}${USER_ID2}",
            USER2.getFullName(),
            USER_ID1,
            TestLifecycleOwner(),
            null,
            firestore,
            firebaseAuth,
            firebaseStorage
        )

        runBlocking {
            delay(1000)
        }

        assertThat(BrugDataCache.getCachedMessageList("${USER_ID1}${USER_ID2}").value.isNullOrEmpty(), IsEqual(false))
        for(value in BrugDataCache.getCachedMessageList("${USER_ID1}${USER_ID2}").value!!){
            Log.e("FIREBASE MESSAGE", value.toString())
        }
        assertThat(BrugDataCache.getCachedMessageList("${USER_ID1}${USER_ID2}").value!!.contains(LOCATIONMSG), IsEqual(true))
    }

    @Test
    fun addPicMessageWithoutLoginReturnsError() = runBlocking {
        val picMsg = PicMessage(
            "Me",
            DateService.fromLocalDateTime(LocalDateTime.now()),
            "PicMessage",
            "")

        val response = MessageRepository.addMessageToConv(
            picMsg,
            false,
            USER_ID1,
            "${USER_ID1}${USER_ID2}",
            firestore,
            firebaseAuth,
            firebaseStorage
        )
        assertThat(response.onError, IsNot(IsNull.nullValue()))
        assertThat(response.onError!!.message, IsEqual("Unable to upload file"))
    }

    @Test
    fun addPicMessageWithLoginCorrectlyAddsPicMessage() = runBlocking {
        BrugDataCache.initMessageListInCache("${USER_ID1}${USER_ID2}")

        // CREATE IMAGE & MESSAGE
        val filePath = getUriOfFileWithImg(R.drawable.ic_baseline_delete_24)
        assertThat(filePath, IsNot(IsNull.nullValue()))

        val picMsg = PicMessage(
            "Me",
            DateService.fromLocalDateTime(LocalDateTime.now()),
            "PicMessage",
            filePath.toString())


        // AUTHENTICATE USER TO FIREBASE TO BE ABLE TO USE FIREBASE STORAGE
        firebaseAuth.createUserWithEmailAndPassword(userEmail, "123456").await()
        val authUser = firebaseAuth
            .signInWithEmailAndPassword(userEmail, "123456")
            .await()
            .user
        assertThat(firebaseAuth.currentUser, IsNot(IsNull.nullValue()))
        assertThat(firebaseAuth.currentUser!!.uid, IsEqual(authUser!!.uid))

        // ADD MESSAGE TO DATABASE & IMAGE TO STORAGE + SIGNOUT
        val response = MessageRepository.addMessageToConv(
            picMsg,
            false,
            USER_ID1,
            "${USER_ID1}${USER_ID2}",
            firestore,
            firebaseAuth,
            firebaseStorage
        )
        assertThat(response.onSuccess, IsEqual(true))

        MessageRepository.getRealtimeMessages(
            "${USER_ID1}${USER_ID2}",
            "USERNAME",
            USER_ID1,
            TestLifecycleOwner(),
            null,
            firestore,
            firebaseAuth,
            firebaseStorage
        )

        delay(2000)

        assertThat(BrugDataCache.getCachedMessageList("${USER_ID1}${USER_ID2}").value.isNullOrEmpty(), IsEqual(false))
        firebaseAuth.signOut()
    }


    @Test
    fun addAudioMessageWithoutLoginReturnsError() = runBlocking {
        val audioMsg = AudioMessage(USER2.getFullName(),
            DateService.fromLocalDateTime(LocalDateTime.now()),
            "AudioMessage",
            "", "")

        val response = MessageRepository.addMessageToConv(
            audioMsg,
            false,
            USER2.uid,
            "${USER_ID1}${USER_ID2}",
            firestore,
            firebaseAuth,
            firebaseStorage
        )

        assertThat(response.onError, IsNot(IsNull.nullValue()))
        assertThat(response.onError!!.message, IsEqual("Unable to upload file"))
    }


    private fun getUriOfFileWithImg(drawableID: Int): Uri? = try {
        val drawable = ApplicationProvider.getApplicationContext<Context>().resources
            .getDrawable(drawableID, null)
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        val file = File.createTempFile("dummyIMG", ".jpg")
        val fos = FileOutputStream(file)
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()

        Log.e("FILE CHECK", file.length().toString())

        Uri.fromFile(file)
    } catch (e: Exception) {
        Log.e("BITMAP ERROR", e.message.toString())
        null
    }
}
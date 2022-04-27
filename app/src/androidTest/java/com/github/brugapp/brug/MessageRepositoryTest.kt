package com.github.brugapp.brug

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.github.brugapp.brug.data.ConvRepository
import com.github.brugapp.brug.data.MessageRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.data.BrugSignInAccount
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.MyUser
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
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
private val ACCOUNT1 = BrugSignInAccount("Rayan", "Kikou", "", "")
private val ACCOUNT2 = BrugSignInAccount("Hamza", "Hassoune", "", "")

private val USER2 = MyUser(USER_ID2, ACCOUNT2.firstName, ACCOUNT2.lastName, null)
private const val DUMMY_ITEM_NAME = "AirPods Pro Max"
private const val CONV_ASSETS = "conversations_assets/"

private val TEXTMSG = Message("Me",
    DateService.fromLocalDateTime(LocalDateTime.now()),
    "TextMessage")

private val LOCATIONMSG = LocationMessage(
    USER2.getFullName(),
    DateService.fromLocalDateTime(LocalDateTime.now()),
    "LocationMessage",
    LocationService.fromGeoPoint(GeoPoint(24.5, 10.8)))


//TODO: TEST AUDIOMESSAGE WHEN IMPLEMENTATION IS COMPLETE
private val AUDIOMSG = AudioMessage(
    USER2.getFullName(),
    DateService.fromLocalDateTime(LocalDateTime.now()),
    "LocationMessage",
    "")


class MessageRepositoryTest {
    private fun addUsersAndConv() = runBlocking {
        UserRepository.addUserFromAccount(USER_ID1, ACCOUNT1)
        UserRepository.addUserFromAccount(USER_ID2, ACCOUNT2)
        ConvRepository.addNewConversation(USER_ID1, USER_ID2, DUMMY_ITEM_NAME)
    }

    @Before
    fun setUp(){
        addUsersAndConv()
    }

    @Test
    fun addMessageToWrongConvReturnsError() = runBlocking {
        val response = MessageRepository.addMessageToConv(
            TEXTMSG,
            USER_ID1,
            "WRONGCONVID")
        assertThat(response.onError, IsNot(IsNull.nullValue()))
    }

    @Test
    fun addTextMessageCorrectlyAddsNewTextMessage() = runBlocking {
        val response = MessageRepository.addMessageToConv(
            TEXTMSG,
            USER_ID1,
            "${USER_ID1}${USER_ID2}")
        assertThat(response.onSuccess, IsEqual(true))

        val conv = ConvRepository.getUserConvFromUID(USER_ID1)!!.filter {
            it.convId == "${USER_ID1}${USER_ID2}"
        }
        assertThat(conv.isNullOrEmpty(), IsEqual(false))
        assertThat(conv[0].messages.contains(TEXTMSG), IsEqual(true))
    }

    @Test
    fun addLocationMessageCorrectlyAddsNewLocationMessage() = runBlocking {
        val response = MessageRepository.addMessageToConv(
            LOCATIONMSG,
            USER_ID2,
            "${USER_ID1}${USER_ID2}")
        assertThat(response.onSuccess, IsEqual(true))

        val conv = ConvRepository.getUserConvFromUID(USER_ID1)!!.filter {
            it.convId == "${USER_ID1}${USER_ID2}"
        }
        assertThat(conv.isNullOrEmpty(), IsEqual(false))

        assertThat(conv[0].messages.contains(LOCATIONMSG), IsEqual(true))
    }

    @Test
    fun addPicMessageWithoutLoginReturnsError() = runBlocking {
        val PICMSG = PicMessage(
            "Me",
            DateService.fromLocalDateTime(LocalDateTime.now()),
            "PicMessage",
            "")

        val response = MessageRepository.addMessageToConv(
            PICMSG,
            USER_ID1,
            "${USER_ID1}${USER_ID2}")
        assertThat(response.onError, IsNot(IsNull.nullValue()))
        assertThat(response.onError!!.message, IsEqual("Unable to upload file"))
    }

    @Test
    fun addPicMessageWithLoginCorrectlyAddsPicMessage() = runBlocking {
        // CREATE IMAGE & MESSAGE
        val filePath = getUriOfFileWithImg(R.drawable.ic_baseline_delete_24)
        assertThat(filePath, IsNot(IsNull.nullValue()))

        val picMsg = PicMessage(
            "Me",
            DateService.fromLocalDateTime(LocalDateTime.now()),
            "PicMessage",
            filePath.toString())


        // AUTHENTICATE USER TO FIREBASE TO BE ABLE TO USE FIREBASE STORAGE
        val authUser = Firebase.auth
            .signInWithEmailAndPassword("test@unlost.com", "123456")
            .await()
            .user
        assertThat(Firebase.auth.currentUser, IsNot(IsNull.nullValue()))
        assertThat(Firebase.auth.currentUser!!.uid, IsEqual(authUser!!.uid))

        // ADD MESSAGE TO DATABASE & IMAGE TO STORAGE + SIGNOUT
        val response = MessageRepository.addMessageToConv(
            picMsg,
            USER_ID1,
            "${USER_ID1}${USER_ID2}")
        assertThat(response.onSuccess, IsEqual(true))

        // CHECK IF MESSAGE HAS BEEN ADDED CORRECTLY
        val conv = ConvRepository.getUserConvFromUID(USER_ID1)!!.filter {
            it.convId == "${USER_ID1}${USER_ID2}"
        }
        assertThat(conv.isNullOrEmpty(), IsEqual(false))
//        val splitPath = filePath.toString().split("/")
//        val firebasePicMessage = PicMessage(picMsg.senderName,
//            picMsg.timestamp,
//            picMsg.body,
//            "${CONV_ASSETS}${conv[0].convId}/${splitPath[splitPath.size-1]}")
//        assertThat(conv[0].messages.contains(firebasePicMessage), IsEqual(true))
        Firebase.auth.signOut()
    }


    @Test
    fun addAudioMessageWithoutLoginReturnsError() = runBlocking {
        val response = MessageRepository.addMessageToConv(
            AUDIOMSG,
            USER2.uid,
            "${USER_ID1}${USER_ID2}")

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
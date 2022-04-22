package com.github.brugapp.brug

import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.message_types.AudioMessage
import com.github.brugapp.brug.model.message_types.LocationMessage
import com.github.brugapp.brug.model.message_types.PicMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import com.google.firebase.firestore.GeoPoint
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month

class MessageTest {
    @Test
    fun initTextMessageCorrectlyInitializesTextMessage() {
        val senderName = "SENDERNAME"
        val timestamp = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val body = "TESTMESSAGE"

        val m = Message(senderName, timestamp, body)

        assertThat(m.senderName, IsEqual(senderName))
        assertThat(m.timestamp, IsEqual(timestamp))
        assertThat(m.body, IsEqual(body))
    }

    @Test
    fun compareIdenticalTextMessagesReturnsEquality() {
        val senderName = "SENDERNAME"
        val timestamp = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val body = "TESTMESSAGE"

        val m1 = Message(senderName, timestamp, body)
        val m2 = Message(senderName, timestamp, body)

        assertThat(m1, IsEqual(m2))
    }

    @Test
    fun compareAlmostIdenticalTextMessagesReturnsFalse() {
        val senderName = "SENDERNAME"
        val timestamp1 = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val timestamp2 = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 18, 0
        ))
        val body = "TESTMESSAGE"

        val m1 = Message(senderName, timestamp1, body)
        val m2 = Message(senderName, timestamp2, body)

        assertThat(m1, IsNot(IsEqual(m2)))
    }

    @Test
    fun initLocationMessageCorrectlyInitializesLocationMessage() {
        val senderName = "SENDERNAME"
        val timestamp = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val body = "TESTMESSAGE"
        val location = LocationService.fromGeoPoint(GeoPoint(0.0, 0.0))

        val m = LocationMessage(senderName, timestamp, body, location)

        assertThat(m.senderName, IsEqual(senderName))
        assertThat(m.timestamp, IsEqual(timestamp))
        assertThat(m.body, IsEqual(body))
        assertThat(m.location, IsEqual(location))
    }

    @Test
    fun initLocationMessageFromTextMessageCorrectlyInitializesLocationMessage() {
        val txtMsg = Message(
            "SENDERNAME",
            DateService.fromLocalDateTime(LocalDateTime.of(
                2022, Month.APRIL, 22, 17, 55
            )),
            "TESTMESSAGE"
        )

        val location = LocationService.fromGeoPoint(GeoPoint(0.0, 0.0))

        val m = LocationMessage.fromTextMessage(txtMsg, location)

        assertThat(m.senderName, IsEqual(txtMsg.senderName))
        assertThat(m.timestamp, IsEqual(txtMsg.timestamp))
        assertThat(m.body, IsEqual(txtMsg.body))
        assertThat(m.location, IsEqual(location))
    }

    @Test
    fun compareIdenticalLocationMessagesReturnsEquality() {
        val senderName = "SENDERNAME"
        val timestamp = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val body = "TESTMESSAGE"
        val location = LocationService.fromGeoPoint(GeoPoint(0.0, 0.0))

        val m1 = LocationMessage(senderName, timestamp, body, location)
        val m2 = LocationMessage(senderName, timestamp, body, location)

        assertThat(m1, IsEqual(m2))
    }

    @Test
    fun compareAlmostIdenticalLocationMessagesReturnsFalse() {
        val senderName = "SENDERNAME"
        val timestamp1 = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val timestamp2 = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 18, 0
        ))
        val body = "TESTMESSAGE"
        val location = LocationService.fromGeoPoint(GeoPoint(0.0, 0.0))

        val m1 = LocationMessage(senderName, timestamp1, body, location)
        val m2 = LocationMessage(senderName, timestamp2, body, location)

        assertThat(m1, IsNot(IsEqual(m2)))
    }

    @Test
    fun initPicMessageCorrectlyInitializesPicMessage() {
        val senderName = "SENDERNAME"
        val timestamp = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val body = "TESTMESSAGE"
        val imgUrl = "/path/to/dummy/img.jpg"

        val m = PicMessage(senderName, timestamp, body, imgUrl)

        assertThat(m.senderName, IsEqual(senderName))
        assertThat(m.timestamp, IsEqual(timestamp))
        assertThat(m.body, IsEqual(body))
        assertThat(m.imgUrl, IsEqual(imgUrl))
    }

    @Test
    fun initPicMessageFromTextMessageCorrectlyInitializesPicMessage() {
        val txtMsg = Message(
            "SENDERNAME",
            DateService.fromLocalDateTime(LocalDateTime.of(
                2022, Month.APRIL, 22, 17, 55
            )),
            "TESTMESSAGE"
        )

        val imgUrl = "/path/to/dummy/img.jpg"

        val m = PicMessage.fromTextMessage(txtMsg, imgUrl)

        assertThat(m.senderName, IsEqual(txtMsg.senderName))
        assertThat(m.timestamp, IsEqual(txtMsg.timestamp))
        assertThat(m.body, IsEqual(txtMsg.body))
        assertThat(m.imgUrl, IsEqual(imgUrl))
    }

    @Test
    fun compareIdenticalPicMessagesReturnsEquality() {
        val senderName = "SENDERNAME"
        val timestamp = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val body = "TESTMESSAGE"
        val picUrl = "/path/to/dummy/img.jpg"

        val m1 = PicMessage(senderName, timestamp, body, picUrl)
        val m2 = PicMessage(senderName, timestamp, body, picUrl)

        assertThat(m1, IsEqual(m2))
    }

    @Test
    fun compareAlmostIdenticalPicMessagesReturnsFalse() {
        val senderName = "SENDERNAME"
        val timestamp1 = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val timestamp2 = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 18, 0
        ))
        val body = "TESTMESSAGE"
        val picUrl = "/path/to/dummy/img.jpg"

        val m1 = PicMessage(senderName, timestamp1, body, picUrl)
        val m2 = PicMessage(senderName, timestamp2, body, picUrl)

        assertThat(m1, IsNot(IsEqual(m2)))
    }

    @Test
    fun initAudioMessageCorrectlyInitializesAudioMessage() {
        val senderName = "SENDERNAME"
        val timestamp = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val body = "TESTMESSAGE"
        val audioUrl = "/path/to/dummy/audio.mp3"

        val m = AudioMessage(senderName, timestamp, body, audioUrl)

        assertThat(m.senderName, IsEqual(senderName))
        assertThat(m.timestamp, IsEqual(timestamp))
        assertThat(m.body, IsEqual(body))
        assertThat(m.audioUrl, IsEqual(audioUrl))
    }

    @Test
    fun initAudioMessageFromTextMessageCorrectlyInitializesAudioMessage() {
        val txtMsg = Message(
            "SENDERNAME",
            DateService.fromLocalDateTime(LocalDateTime.of(
                2022, Month.APRIL, 22, 17, 55
            )),
            "TESTMESSAGE"
        )

        val audioUrl = "/path/to/dummy/audio.mp3"

        val m = AudioMessage.fromTextMessage(txtMsg, audioUrl)

        assertThat(m.senderName, IsEqual(txtMsg.senderName))
        assertThat(m.timestamp, IsEqual(txtMsg.timestamp))
        assertThat(m.body, IsEqual(txtMsg.body))
        assertThat(m.audioUrl, IsEqual(audioUrl))
    }

    @Test
    fun compareIdenticalAudioMessagesReturnsEquality() {
        val senderName = "SENDERNAME"
        val timestamp = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val body = "TESTMESSAGE"
        val audioUrl = "/path/to/dummy/audio.mp3"

        val m1 = AudioMessage(senderName, timestamp, body, audioUrl)
        val m2 = AudioMessage(senderName, timestamp, body, audioUrl)

        assertThat(m1, IsEqual(m2))
    }

    @Test
    fun compareAlmostIdenticalAudioMessagesReturnsFalse() {
        val senderName = "SENDERNAME"
        val timestamp1 = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 17, 55
        ))
        val timestamp2 = DateService.fromLocalDateTime(LocalDateTime.of(
            2022, Month.APRIL, 22, 18, 0
        ))
        val body = "TESTMESSAGE"
        val audioUrl = "/path/to/dummy/audio.mp3"

        val m1 = AudioMessage(senderName, timestamp1, body, audioUrl)
        val m2 = AudioMessage(senderName, timestamp2, body, audioUrl)

        assertThat(m1, IsNot(IsEqual(m2)))
    }
}
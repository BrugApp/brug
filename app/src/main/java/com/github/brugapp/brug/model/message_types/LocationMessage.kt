package com.github.brugapp.brug.model.message_types

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.github.brugapp.brug.model.Message
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.model.services.LocationService
import java.io.File
import java.io.FileOutputStream
import java.util.*

data class LocationMessage(
    override val senderName: String,
    override val timestamp: DateService,
    override val body: String,
    val location: LocationService
): Message(senderName, timestamp, body) {
    private val mapUrl = MutableLiveData<Uri>()

    companion object {
        fun fromMessage(m: Message, location: LocationService): LocationMessage {
            return LocationMessage(
                m.senderName,
                m.timestamp,
                m.body,
                location
            )
        }
    }

    fun getImageUri(): MutableLiveData<Uri>{
        return this.mapUrl
    }

    fun setImageUri(uri: Uri){
        mapUrl.postValue(uri)
    }
}

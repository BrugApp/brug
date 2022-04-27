package com.github.brugapp.brug.model.services

import com.google.firebase.Timestamp
import java.io.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Abstraction class handling dates.
 */
class DateService private constructor(private val seconds: Long): Serializable {
    companion object{
        fun fromFirebaseTimestamp(timestamp: Timestamp): DateService {
            return DateService(timestamp.seconds)
        }

        fun fromLocalDateTime(datetime: LocalDateTime): DateService{
            return DateService(datetime.toEpochSecond(ZoneOffset.UTC))
        }
    }

    fun toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.of("UTC"))
    }

    fun toFirebaseTimestamp(): Timestamp {
        return Timestamp(seconds, 0)
    }

    fun getSeconds(): Long{
        return seconds
    }

    override fun equals(other: Any?): Boolean {
        return this.seconds == (other as DateService).seconds
    }

    override fun hashCode(): Int {
        return seconds.hashCode()
    }

}
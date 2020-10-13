package me.deprilula28.discordproxykt.entities

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

data class Timestamp(val unixMillis: Long) {
    companion object {
        private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'")
        init {
            format.timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    
    val offsetDateTime: OffsetDateTime
        get() = OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(unixMillis),
            ZoneId.systemDefault(),
        )
    
    override fun toString(): String = format.format(Date())
}
package me.deprilula28.discordproxykt.entities

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class Timestamp(val unixMillis: Long) {
    val offsetDateTime: OffsetDateTime
        get() = OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(unixMillis),
            ZoneId.systemDefault(),
        )
}
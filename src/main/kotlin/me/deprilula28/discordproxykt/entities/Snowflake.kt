package me.deprilula28.discordproxykt.entities

class Snowflake(val id: String) {
    val idLong: Long by lazy { id.toLong() }
}

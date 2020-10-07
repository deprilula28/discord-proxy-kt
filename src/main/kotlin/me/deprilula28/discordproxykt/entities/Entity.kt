package me.deprilula28.discordproxykt.entities

import me.deprilula28.discordproxykt.JdaProxySpectacles
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.deprilula28.discordproxykt.entities.discord.message.Emoji
import me.deprilula28.discordproxykt.entities.discord.message.ReactionEmoji
import me.deprilula28.discordproxykt.entities.discord.message.UnicodeEmoji
import java.awt.Color
import java.time.ZonedDateTime
import java.util.*

open class Entity(private val map: JsonObject, val bot: JdaProxySpectacles) {
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot"))
    val jda: JdaProxySpectacles by ::bot
    
    val snowflake: Snowflake by lazy { map["id"]!!.asSnowflake() }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.id"))
    val id: String by lazy { snowflake.id }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.idLong"))
    val idLong: Long by lazy { snowflake.idLong }
}

fun JsonElement.asString() = (this as JsonPrimitive).content
fun JsonElement.asInt() = asString().toInt()
fun JsonElement.asLong() = asString().toLong()
fun JsonElement.asBoolean() = asString().toBoolean()
fun JsonElement.asSnowflake() = Snowflake(asString())
fun JsonElement.asColor() = Color(asInt())
fun JsonElement.asTimestamp() = Timestamp(Date.from(ZonedDateTime.parse(asString()).toInstant()).time)

// https://stackoverflow.com/questions/5346477/implementing-a-bitfield-using-java-enums
inline fun <reified E: Enum<E>> Long.bitSetToEnumSet(values: Array<E>): EnumSet<E> {
    val enumSet = EnumSet.noneOf(E::class.java)
    values.forEach {
        val flag = 1L shl it.ordinal
        if ((flag and this@bitSetToEnumSet) == flag) enumSet.add(it)
    }
    return enumSet
}

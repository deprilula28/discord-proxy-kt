package me.deprilula28.discordproxykt.entities

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.deprilula28.discordproxykt.DiscordProxyKt
import java.awt.Color
import java.time.ZonedDateTime
import java.util.*
import kotlin.properties.ReadOnlyProperty

open class Entity(private val map: JsonObject, val bot: DiscordProxyKt) {
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot"))
    val jda: DiscordProxyKt by ::bot
    
    val snowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "id")
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.id"))
    val id: String by snowflake::id
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.idLong"))
    val idLong: Long by snowflake::idLong
}

fun JsonElement.asString() = (this as JsonPrimitive).content
fun JsonElement.asInt() = asString().toInt()
fun JsonElement.asLong() = asString().toLong()
fun JsonElement.asBoolean() = asString().toBoolean()
fun JsonElement.asSnowflake() = Snowflake(asString())
fun JsonElement.asColor() = Color(asInt())
fun JsonElement.asTimestamp(): Timestamp {
    return Timestamp(Date.from(ZonedDateTime.parse(asString()).toInstant()).time)
}

inline fun <reified T> JsonObject.delegateJson(crossinline func: JsonElement.() -> T, field: String? = null): ReadOnlyProperty<Any?, T>
    = ReadOnlyProperty { _, property ->
        func(this[field ?: property.name]!!)
    }

inline fun <reified T> JsonObject.delegateJsonNullable(crossinline func: JsonElement.() -> T?, field: String? = null): ReadOnlyProperty<Any?, T?>
    = ReadOnlyProperty { _, property ->
        val prop = this[field ?: property.name]
        if (prop == null || prop == JsonNull || prop.toString() == "null") null
        else func(prop)
    }

// https://stackoverflow.com/questions/5346477/implementing-a-bitfield-using-java-enums
inline fun <reified E: Enum<E>> Long.bitSetToEnumSet(values: Array<E>): EnumSet<E> {
    val enumSet = EnumSet.noneOf(E::class.java)
    values.forEach {
        val flag = 1L shl it.ordinal
        if ((flag and this@bitSetToEnumSet) == flag) enumSet.add(it)
    }
    return enumSet
}

package me.deprilula28.discordproxykt.entities

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.rest.IRestAction
import me.deprilula28.discordproxykt.rest.RestAction
import java.awt.Color
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/// Interfaces deriving this only need the ID to run the included functions.
interface IPartialEntity {
    val snowflake: Snowflake
    val bot: DiscordProxyKt
}

interface EntityManager<T>: IRestAction<T> {
    val edits: MutableMap<String, JsonElement>
}

/// Classes deriving this will be Discord entities, with an ID and delegated fields.
open class Entity(private val map: JsonObject, override val bot: DiscordProxyKt): IPartialEntity {
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot")) val jda: DiscordProxyKt by ::bot
    
    override val snowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "id")
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.id")) val id: String by snowflake::id
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.idLong")) val idLong: Long by snowflake::idLong
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

inline fun <reified T> getValue(obj: JsonObject, field: String, crossinline func: JsonElement.() -> T): T = func(
    obj[field]!!)

inline fun <reified T> getValueNullable(obj: JsonObject, field: String, crossinline func: JsonElement.() -> T): T? {
    val prop = obj[field]
    return if (prop == null || prop == JsonNull || prop.toString() == "null") null
    else func(prop)
}

inline fun <reified T> JsonObject.delegateJson(
    crossinline func: JsonElement.() -> T,
    field: String? = null,
): ReadOnlyProperty<Any?, T> = ReadOnlyProperty { _, property -> getValue(this, field ?: property.name, func) }

inline fun <reified T> JsonObject.delegateJsonNullable(
    crossinline func: JsonElement.() -> T?,
    field: String? = null,
): ReadOnlyProperty<Any?, T?> = ReadOnlyProperty { _, property ->
    getValueNullable(this@delegateJsonNullable, field ?: property.name, func)
}

inline fun <reified T> JsonObject.delegateJsonMutable(
    crossinline read: JsonElement.() -> T,
    crossinline write: (T) -> JsonElement,
    field: String? = null,
): ReadWriteProperty<EntityManager<*>, T> = object: ReadWriteProperty<EntityManager<*>, T> {
    override fun getValue(thisRef: EntityManager<*>, property: KProperty<*>): T = getValue(this@delegateJsonMutable,
                                                                                        field ?: property.name, read)
    
    override fun setValue(thisRef: EntityManager<*>, property: KProperty<*>, value: T) {
        thisRef.edits[field ?: property.name] = write(value)
    }
}

inline fun <reified T> JsonObject.delegateJsonMutableNullable(
    crossinline read: JsonElement.() -> T?,
    crossinline write: (T) -> JsonElement,
    field: String? = null,
): ReadWriteProperty<EntityManager<*>, T?> = object: ReadWriteProperty<EntityManager<*>, T?> {
    override fun getValue(thisRef: EntityManager<*>, property: KProperty<*>): T? = getValueNullable(
        this@delegateJsonMutableNullable, field ?: property.name, read)
    
    override fun setValue(thisRef: EntityManager<*>, property: KProperty<*>, value: T?) {
        thisRef.edits[field ?: property.name] = write(value!!)
    }
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

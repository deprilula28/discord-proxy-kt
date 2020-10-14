package me.deprilula28.discordproxykt.rest

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.UnavailableField
import me.deprilula28.discordproxykt.entities.discord.MemberOverride
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PermissionOverwrite
import me.deprilula28.discordproxykt.entities.discord.RoleOverride
import me.deprilula28.discordproxykt.entities.discord.channel.*
import java.awt.Color
import java.time.ZonedDateTime
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface EntityManager<T> {
    val changes: MutableMap<String, JsonElement>
    fun edit(): IRestAction<T>
    
    fun resetChanges() = changes.clear()
}

interface EntityBuilder<T> {
    fun create(): IRestAction<T>
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
fun JsonElement.asGuildChannel(bot: DiscordProxyKt, guild: PartialGuild): GuildChannel? {
    val obj = this as JsonObject
    return when (val type = obj["type"]!!.asInt()) {
        0 -> TextChannel(guild, obj, bot)
        2 -> VoiceChannel(obj, bot)
        4 -> Category(obj, bot)
        else -> {
            println("Invalid channel type received: $type")
            null
        }
    }
}
fun JsonElement.asMessageChannel(bot: DiscordProxyKt, guild: PartialGuild): MessageChannel? {
    val obj = this as JsonObject
    return when (val type = obj["type"]!!.asInt()) {
        0 -> TextChannel(guild, obj, bot)
        1 -> PrivateChannel(obj, bot)
        else -> {
            println("Invalid channel type received: $type")
            null
        }
    }
}
fun JsonElement.asPermissionOverwrite(channel: GuildChannel, guild: PartialGuild, bot: DiscordProxyKt): PermissionOverwrite {
    val obj = this as JsonObject
    return when (val type = obj["type"]!!.asInt()) {
        0 -> RoleOverride(guild, channel, obj, bot)
        1 -> MemberOverride(guild, channel, obj, bot)
        else -> throw InternalError("Invalid permission overwrite type received: $type")
    }
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
        thisRef.changes[field ?: property.name] = write(value)
    }
}

inline fun <reified T> JsonObject.delegateJsonMutableNullable(
    crossinline read: JsonElement.() -> T?,
    crossinline write: (T?) -> JsonElement,
    field: String? = null,
): ReadWriteProperty<EntityManager<*>, T?> = object: ReadWriteProperty<EntityManager<*>, T?> {
    override fun getValue(thisRef: EntityManager<*>, property: KProperty<*>): T? = getValueNullable(
        this@delegateJsonMutableNullable, field ?: property.name, read)
    
    override fun setValue(thisRef: EntityManager<*>, property: KProperty<*>, value: T?) {
        thisRef.changes[field ?: property.name] = write(value)
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

fun <E: Enum<E>> EnumSet<E>.toBitSet(): Long {
    var num = 0L
    forEach { num = num and (1L shl it.ordinal) }
    return num
}

class MapNotReady<K, V>: Map<K, V> {
    override val entries: Set<Map.Entry<K, V>>
        get() = throw UnavailableField()
    override val keys: Set<K>
        get() = throw UnavailableField()
    override val size: Int
        get() = throw UnavailableField()
    override val values: Collection<V>
        get() = throw UnavailableField()
    
    override fun containsKey(key: K): Boolean = throw UnavailableField()
    override fun containsValue(value: V): Boolean = throw UnavailableField()
    override fun get(key: K): V? = throw UnavailableField()
    override fun isEmpty(): Boolean = throw UnavailableField()
}

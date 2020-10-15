package me.deprilula28.discordproxykt.rest

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Parse
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

interface EntityManager<T>: Parse {
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
    return when (val type = obj["type"]!!.asString()) {
        "role", "0" -> RoleOverride(guild, channel, obj, bot)
        "member", "1" -> MemberOverride(guild, channel, obj, bot)
        else -> throw InternalError("Invalid permission overwrite type received: $type")
    }
}

fun <T> getValue(obj: JsonObject, field: String, func: JsonElement.() -> T): T = func(obj[field]!!)

fun <T> getValueNullable(obj: JsonObject, field: String, func: JsonElement.() -> T): T? {
    val prop = obj[field]
    return if (prop == null || prop == JsonNull || prop.toString() == "null") null
    else func(prop)
}

class MapDelegate<T>(val field: String?, val func: JsonElement.() -> T): ReadOnlyProperty<Parse, T> {
    override fun getValue(thisRef: Parse, property: KProperty<*>): T = getValue(thisRef.map, field ?: property.name, func)
}

fun <T> parsing(
    func: JsonElement.() -> T,
    field: String? = null,
): ReadOnlyProperty<Parse, T> = MapDelegate(field, func)

class MapDelegateNullable<T>(val field: String?, val func: JsonElement.() -> T?): ReadOnlyProperty<Parse, T?> {
    override fun getValue(thisRef: Parse, property: KProperty<*>): T? = getValueNullable(thisRef.map, field ?: property.name, func)
}

fun <T> parsingOpt(
    func: JsonElement.() -> T?,
    field: String? = null,
): ReadOnlyProperty<Parse, T?> = MapDelegateNullable(field, func)

class MapDelegateMutable<T>(val field: String?, val read: JsonElement.() -> T, val write: (T) -> JsonElement): ReadWriteProperty<EntityManager<*>, T> {
    override fun getValue(thisRef: EntityManager<*>, property: KProperty<*>): T = getValue(thisRef.map, field ?: property.name, read)
    override fun setValue(thisRef: EntityManager<*>, property: KProperty<*>, value: T) {
        thisRef.changes[field ?: property.name] = write(value)
    }
}

fun <T> parsing(
    read: JsonElement.() -> T,
    write: (T) -> JsonElement,
    field: String? = null,
) = MapDelegateMutable(field, read, write)

class MapDelegateMutableNullable<T>(val field: String?, val read: JsonElement.() -> T?, val write: (T?) -> JsonElement): ReadWriteProperty<EntityManager<*>, T?> {
    override fun getValue(thisRef: EntityManager<*>, property: KProperty<*>): T? = getValueNullable(thisRef.map, field ?: property.name, read)
    override fun setValue(thisRef: EntityManager<*>, property: KProperty<*>, value: T?) {
        thisRef.changes[field ?: property.name] = write(value)
    }
}

fun <T> parsingOpt(
    read: JsonElement.() -> T?,
    write: (T?) -> JsonElement,
    field: String? = null,
) = MapDelegateMutableNullable(field, read, write)

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
    forEach { num = num or (1L shl it.ordinal) }
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

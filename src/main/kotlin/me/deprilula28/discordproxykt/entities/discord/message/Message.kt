package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.JdaProxySpectacles
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.Member
import me.deprilula28.discordproxykt.entities.discord.User
import java.time.OffsetDateTime
import java.util.*

// https://discord.com/developers/docs/resources/channel#message-object
class Message(map: JsonObject, bot: JdaProxySpectacles): Entity(map, bot) {
    val channelSnowflake: Snowflake by lazy { map["channel_id"]!!.asSnowflake() }
    val guildSnowflake: Snowflake? by lazy { map["guild_id"]?.asSnowflake() }
    val author: User by lazy { User(map["author"] as JsonObject, bot) }
    val member: Member? by lazy { map["member"]?.run { Member(this as JsonObject, bot, author) } }
    val content: String by lazy { map["content"]!!.asString() }
    val timestamp: Timestamp by lazy { map["timestamp"]!!.asTimestamp() }
    val editTimestamp: Timestamp? by lazy { map["edited_timestamp"]?.asTimestamp() }
    val tts: Boolean by lazy { map["tts"]!!.asBoolean() }
    val mentionEveryone: Boolean by lazy { map["mention_everyone"]!!.asBoolean() }
    val mentions: List<User> by lazy { (map["mentions"] as JsonArray).map { User(it as JsonObject, bot) } }
    val mentionRoles: List<Snowflake> by lazy { (map["mention_roles"] as JsonArray).map { it.asSnowflake() } }
    val mentionChannels: List<Snowflake> by lazy { (map["mention_channels"] as JsonArray).map { (it as JsonObject)["id"]!!.asSnowflake() } }
    val attachments: List<Attachment> by lazy { (map["attachments"] as JsonArray).map { Attachment(it as JsonObject, bot) } }
    val embeds: List<Embed> by lazy { (map["embeds"] as JsonArray).map { Embed(it as JsonObject, bot) } }
    val reactions: List<Reaction> by lazy { (map["reactions"] as JsonArray).map { Reaction(it as JsonObject, bot) } }
    val pinned: Boolean by lazy { map["pinned"]!!.asBoolean() }
    val webhookId: Snowflake? by lazy { map["webhook_id"]?.asSnowflake() }
    val type: Type by lazy { Type.values()[map["type"]!!.asInt()] }
    val flags: EnumSet<Flags> by lazy { map["flags"]!!.asLong().bitSetToEnumSet(Flags.values()) }
    
    // JDA Compatibility
    @Deprecated("JDA Compatibility Field", ReplaceWith("mentions"))
    val mentionedUsers: List<User>
        get() = mentions
    
    //TODO Fetch these
//    @Deprecated("JDA Compatibility Field", ReplaceWith("mentionChannels"))
//    val mentionedChannels: List<MessageChannel>
//        get() = mentionChannels
//    @Deprecated("JDA Compatibility Field", ReplaceWith("mentionRoles"))
//    val mentionedRoles: List<Role>
//        get() = mentionRoles
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("editTimestamp.offsetDateTime"))
    val timeEdited: OffsetDateTime?
        get() = editTimestamp?.offsetDateTime
    
    enum class Flags {
        CROSSPOSTED,
        IS_CROSSPOST,
        SUPPRESS_EMBEDS,
        SOURCE_MESSAGE_DELETED,
        URGENT,
    }
    enum class Type {
        DEFAULT,
        RECIPIENT_ADD,
        RECIPIENT_REMOVE,
        CALL,
        CHANNEL_NAME_CHANGE,
        CHANNEL_ICON_CHANGE,
        CHANNEL_PINNED_MESSAGE,
        GUILD_MEMBER_JOIN,
        USER_PREMIUM_GUILD_SUBSCRIPTION,
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1,
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2,
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3,
        CHANNEL_FOLLOW_ADD,
        GUILD_DISCOVERY_DISQUALIFIED,
        GUILD_DISCOVERY_REQUALIFIED,
    }
}
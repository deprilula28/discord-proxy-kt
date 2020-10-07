package me.deprilula28.discordproxykt.entities.discord.message

import me.deprilula28.discordproxykt.DiscordProxyKt
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.Member
import me.deprilula28.discordproxykt.entities.discord.User
import java.time.OffsetDateTime
import java.util.*

// https://discord.com/developers/docs/resources/channel#message-object
class Message(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    val channelSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "channel_id")
    val guildSnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "guild_id")
    val author: User by map.delegateJson({ User(this as JsonObject, bot) })
    val member: Member? by map.delegateJsonNullable({ Member(this as JsonObject, bot, author) })
    val content: String by map.delegateJson(JsonElement::asString)
    val timestamp: Timestamp by map.delegateJson(JsonElement::asTimestamp)
    val editTimestamp: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "edited_timestamp")
    val tts: Boolean by map.delegateJson(JsonElement::asBoolean)
    val mentionEveryone: Boolean by map.delegateJson(JsonElement::asBoolean, "mention_everyone")
    val mentions: List<User> by map.delegateJson({ (this as JsonArray).map { User(it as JsonObject, bot) } })
    val mentionRoles: List<Snowflake> by map.delegateJson({ (this as JsonArray).map { it.asSnowflake() } }, "mention_roles")
    val mentionChannels: List<Snowflake>? by map.delegateJsonNullable({ (this as JsonArray?)?.map { (it as JsonObject)["id"]!!.asSnowflake() } }, "mention_channels")
    val attachments: List<Attachment> by map.delegateJson({ (this as JsonArray).map { Attachment(it as JsonObject, bot) } })
    val embeds: List<Embed> by map.delegateJson({ (this as JsonArray).map { Embed(it as JsonObject, bot) } })
    val reactions: List<Reaction> by map.delegateJson({ (this as JsonArray).map { Reaction(it as JsonObject, bot) } })
    val pinned: Boolean by map.delegateJson(JsonElement::asBoolean)
    val webhookId: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake)
    val type: Type by map.delegateJson({ Type.values()[asInt()] })
    val flags: EnumSet<Flags> by map.delegateJson({ asLong().bitSetToEnumSet(Flags.values()) })
    
    // JDA Compatibility
    @Deprecated("JDA Compatibility Field", ReplaceWith("mentions")) val mentionedUsers: List<User>
        get() = mentions
    
    //TODO Fetch these
    //    @Deprecated("JDA Compatibility Field", ReplaceWith("mentionChannels"))
    //    val mentionedChannels: List<MessageChannel>
    //        get() = mentionChannels
    //    @Deprecated("JDA Compatibility Field", ReplaceWith("mentionRoles"))
    //    val mentionedRoles: List<Role>
    //        get() = mentionRoles
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("editTimestamp.offsetDateTime")) val timeEdited: OffsetDateTime?
        get() = editTimestamp?.offsetDateTime
    
    enum class Flags {
        CROSSPOSTED, IS_CROSSPOST, SUPPRESS_EMBEDS, SOURCE_MESSAGE_DELETED, URGENT,
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
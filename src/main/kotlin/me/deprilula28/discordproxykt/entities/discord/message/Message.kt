package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.rest.IRestAction
import me.deprilula28.discordproxykt.rest.RestAction
import me.deprilula28.discordproxykt.rest.RestEndpoint
import java.time.OffsetDateTime
import java.util.*

// TODO This
interface PartialMessage: IPartialEntity {
    fun editMessage(text: CharSequence) {}
    fun editMessage(embed: Embed) {}
    fun editMessageFormat(str: String, vararg format: Any) = editMessage(String.format(str, *format))
    
    fun delete() {}
    fun pin() {}
    fun unpin() {}
    
    fun addReaction(emoji: Emoji) {}
    fun addReaction(unicode: String) {}
    
    fun clearReactions() {}
    fun clearReactions(emoji: Emoji) {}
    fun clearReactions(emoji: Emoji, user: User) {} // TODO Make into partial user
    fun clearReactions(unicode: String) {}
    fun clearReactions(unicode: String, user: User) {} // TODO Make into partial user
    
    interface Upgradeable: PartialMessage, IRestAction<Message>
}

// https://discord.com/developers/docs/resources/channel#message-object
class Message(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), PartialMessage {
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
    val mentionRoles: List<PartialRole> = listOf()//TODO by map.delegateJson({ (this as JsonArray).map { it.asSnowflake() } }, "mention_roles")
    val mentionChannels: List<PartialTextChannel>? by map.delegateJsonNullable({ (this as JsonArray?)?.map { bot.channels[(it as JsonObject)["id"]!!.asSnowflake()] } }, "mention_channels")
    val attachments: List<Attachment> by map.delegateJson({ (this as JsonArray).map { Attachment(it as JsonObject, bot) } })
    val embeds: List<Embed> by map.delegateJson({ (this as JsonArray).map { Embed(it as JsonObject, bot) } })
    val reactions: List<Reaction> by map.delegateJson({ (this as JsonArray).map { Reaction(it as JsonObject, bot) } })
    val pinned: Boolean by map.delegateJson(JsonElement::asBoolean)
    val webhookId: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake)
    val type: Type by map.delegateJson({ Type.values()[asInt()] })
    val flags: EnumSet<Flags> by map.delegateJson({ asLong().bitSetToEnumSet(Flags.values()) })
    
    // JDA Compatibility
    fun getMentions(vararg types: MentionType): List<Mentionable> = types.flatMap { it.method(this@Message) }
    @Suppress("UNCHECKED_CAST") fun isMentioned(mentionable: Mentionable, vararg types: List<MentionType>): Boolean
            = getMentions(*types.ifEmpty { MentionType.values() } as Array<out MentionType>).contains(mentionable)
    
    interface Mentionable {
        val asMention: String
    }
    
    enum class MentionType(val method: Message.() -> List<Mentionable>) {
        USER({ mentions }),
        ROLE({ mentionRoles }),
        CHANNEL({ mentionChannels ?: listOf() }),
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("mentions"))
    val mentionedUsers: List<User> by ::mentions
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("mentionEveryone"))
    fun mentionsEveryone() = mentionEveryone
    
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
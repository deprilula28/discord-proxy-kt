package entities.message

import JdaProxySpectacles
import entities.Entity
import entities.Snowflake
import entities.Timestamp
import entities.discord.Member
import entities.discord.MessageChannel
import entities.discord.Role
import entities.discord.User
import java.time.OffsetDateTime
import java.util.*

class Message(
    val id: Snowflake,
    val channelId: Snowflake,
    val guildId: Snowflake?,
    val author: User,
    val member: Member?,
    val content: String,
    val timestamp: Timestamp,
    val editTimestamp: Timestamp,
    val tts: Boolean,
    val mentionEveryone: Boolean,
    val mentions: List<User>,
    val mentionRoles: List<Role>,
    val mentionChannels: List<MessageChannel>,
    val attachments: List<Attachment>,
    val embeds: List<Embed>,
    val reactions: List<Reaction>,
    val pinned: Boolean,
    val webhookId: Snowflake?,
    val type: Type,
    val flags: EnumSet<Flags>,
    bot: JdaProxySpectacles
): Entity(bot) {
    // JDA Compatibility
    @Deprecated("JDA Compatibility Field", ReplaceWith("mentions"))
    val mentionedUsers: List<User>
        get() = mentions
    @Deprecated("JDA Compatibility Field", ReplaceWith("mentionChannels"))
    val mentionedChannels: List<MessageChannel>
        get() = mentionChannels
    @Deprecated("JDA Compatibility Field", ReplaceWith("mentionRoles"))
    val mentionedRoles: List<Role>
        get() = mentionRoles
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("editTimestamp.offsetDateTime"))
    val timeEdited: OffsetDateTime
        get() = editTimestamp.offsetDateTime
    
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
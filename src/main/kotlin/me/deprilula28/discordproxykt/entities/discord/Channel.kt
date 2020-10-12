package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.rest.*

interface PartialMessageChannel: IPartialEntity {
    operator fun get(message: Snowflake): PartialMessage.Upgradeable
    
    interface Upgradeable: PartialMessageChannel
}

/**
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
interface MessageChannel {
    /**
     * when the last pinned message was pinned. This may be null in events such as GUILD_CREATE when a message is not pinned.
     */
    val lastPinTimestamp: Timestamp?
    
    /**
     * the id of the last message sent in this channel (may not point to an existing or valid message)
     */
    val lastMessageId: Snowflake
}

interface PartialGuildChannel: IPartialEntity {
    interface Upgradeable: PartialGuildChannel
}

/**
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
interface GuildChannel: PartialGuildChannel {
    /**
     * the id of the guild
     */
    val guildSnowflake: Snowflake
    /**
     * sorting position of the channel
     */
    val position: Int
    /**
     * the name of the channel (2-100 characters)
     */
    val name: String
    /**
     * explicit permission overwrites for members and roles
     */
    val permissions: List<PermissionOverwrite>
    /**
     * id of the parent category for a channel (each parent category can contain up to 50 channels)
     */
    val categorySnowflake: Snowflake?
    /**
     * Channel Type, should be constant
     */
    val type: ChannelType
}

/**
 * a voice channel within a server
 * <br>
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
interface PartialVoiceChannel: IPartialEntity {
    companion object {
        fun new(guild: PartialGuild, id: Snowflake): Upgradeable
                = object: Upgradeable,
            IRestAction.FuturesRestAction<VoiceChannel>(
                guild.bot,
                { guild.fetchChannels.request().thenApply { it.find { ch -> ch.snowflake == id } as VoiceChannel } }) {
            override val snowflake: Snowflake = id
        }
    }
    
    interface Upgradeable: PartialVoiceChannel, PartialGuildChannel.Upgradeable, IRestAction<VoiceChannel>
}

class VoiceChannel(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), GuildChannel, PartialVoiceChannel {
    /**
     * the bitrate (in bits) of the voice channel
     */
    val bitrate: Int by map.delegateJson(JsonElement::asInt)
    /**
     * the user limit of the voice channel
     */
    val userLimit: Int by map.delegateJson(JsonElement::asInt, "user_limit")
    
    override val guildSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "guild_id")
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val name: String by map.delegateJson(JsonElement::asString)
    override val categorySnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "parent_id")
    
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map {
            PermissionOverwrite(it as JsonObject, bot)
        }
    }, "permission_overwrites")
    
    override val type: ChannelType
        get() = ChannelType.VOICE
}

/**
 * an organizational category that contains up to 50 channels
 * <br>
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
class Category(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), GuildChannel {
    override val guildSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "guild_id")
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val name: String by map.delegateJson(JsonElement::asString)
    override val categorySnowflake: Snowflake? by ::snowflake
    
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map {
            PermissionOverwrite(it as JsonObject, bot)
        }
    }, "permission_overwrites")
    
    override val type: ChannelType
        get() = ChannelType.CATEGORY
}

interface PartialPrivateChannel: PartialMessageChannel, IPartialEntity {
    companion object {
        fun new(id: Snowflake, bot: DiscordProxyKt): Upgradeable
            = object: Upgradeable,
                    RestAction<PrivateChannel>(bot, RestEndpoint.CREATE_DM.path(),
                         { PrivateChannel(this as JsonObject, bot) }, { Json.encodeToString(
                             "recipient_id" to JsonPrimitive(id.id)
                         ) }
                    ) {
                override val snowflake: Snowflake = id
                override val bot: DiscordProxyKt = bot
        }
    }
    
    override operator fun get(message: Snowflake): PartialMessage.Upgradeable = PartialMessage.new(this, message)
    
    interface Upgradeable: PartialPrivateChannel, PartialMessageChannel.Upgradeable, IRestAction<PrivateChannel>
}

/**
 * a direct message between users
 * <br>
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
class PrivateChannel(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), MessageChannel {
    override val lastPinTimestamp: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "last_pin_timestamp")
    override val lastMessageId: Snowflake by map.delegateJson(JsonElement::asSnowflake, "last_message_id")
}

package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.IPartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.ChannelType
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PermissionOverwrite
import me.deprilula28.discordproxykt.rest.*

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
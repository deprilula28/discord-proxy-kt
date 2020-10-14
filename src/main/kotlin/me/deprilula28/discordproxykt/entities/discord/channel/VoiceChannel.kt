package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.PartialEntity
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
interface PartialVoiceChannel: PartialEntity {
    companion object {
        fun new(guild: PartialGuild, id: Snowflake): PartialVoiceChannel
                = object: PartialVoiceChannel {
            override val snowflake: Snowflake = id
            override val bot: DiscordProxyKt = guild.bot
    
            override fun upgrade(): IRestAction<VoiceChannel> =
                IRestAction.FuturesRestAction(guild.bot) {
                    guild.fetchChannels.request().thenApply {
                        it.find { ch -> ch.snowflake == id } as VoiceChannel
                    }
                }
        }
    }
    
    fun upgrade(): IRestAction<VoiceChannel>
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
    
    override val guild: PartialGuild by map.delegateJson({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val name: String by map.delegateJson(JsonElement::asString)
    override val category: PartialCategory? by map.delegateJsonNullable({ PartialCategory.new(guild, asSnowflake()) }, "parent_id")
    
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map { asPermissionOverwrite(this@VoiceChannel, guild, bot) }
    }, "permission_overwrites")
    
    override fun upgrade(): IRestAction<VoiceChannel> = IRestAction.ProvidedRestAction(bot, this)
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("category?.upgrade()?.request()?.get()"))
    val parent: Category?
        get() = category?.upgrade()?.request()?.get()
    
    override val type: ChannelType
        get() = ChannelType.VOICE
}
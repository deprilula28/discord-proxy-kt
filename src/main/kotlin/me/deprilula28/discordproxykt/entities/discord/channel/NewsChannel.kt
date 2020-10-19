package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.Permissions
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.rest.*

/**
 * This type is used for operations when an ID of a [NewsChannel] is known.<br>
 * If the data is also known it will implement [NewsChannel], and [upgrade] is a no-op.<br>
 * If it isn't known, [upgrade] will be a request to get the data from Discord.
 */
interface PartialNewsChannel: PartialTextChannel, PartialMessageChannel, PartialGuildChannel, PartialEntity, Message.Mentionable {
    companion object {
        fun new(guild: PartialGuild, id: Snowflake): PartialNewsChannel
            = object: PartialNewsChannel {
                override val snowflake: Snowflake = id
                override val bot: DiscordProxyKt = guild.bot
                override val internalGuild: PartialGuild = guild
                override fun upgrade(): IRestAction<NewsChannel>
                    = bot.request(RestEndpoint.GET_CHANNEL.path(snowflake.id), { NewsChannel(guild, this as JsonObject, bot) })
                override fun toString(): String = "Channel(partial, $type, $guild, $snowflake.id)"
            }
    }
    
    override fun upgrade(): IRestAction<NewsChannel>
    
    override val type: ChannelType
        get() = ChannelType.NEWS
}

/**
 * a channel that users can follow and crosspost into their own server
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
open class NewsChannel(internalGuild: PartialGuild, map: JsonObject, bot: DiscordProxyKt):
    TextChannel(internalGuild, map, bot), MessageChannel, GuildChannel, PartialTextChannel, EntityManager<TextChannel>
{
    override fun upgrade(): IRestAction<NewsChannel> = IRestAction.ProvidedRestAction(bot, this)
}

class NewsChannelBuilder(guild: PartialGuild, bot: DiscordProxyKt):
    NewsChannel(guild, JsonObject(MapNotReady()), bot), EntityBuilder<NewsChannel>
{
    /**
     * Creates a text channel based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a [NewsChannel].
     */
    override fun create(): IRestAction<NewsChannel> {
        if (!changes.containsKey("name")) throw InvalidRequestException("Channels require at least a name.")
        changes["type"] = JsonPrimitive(5)
        return IRestAction.coroutine(internalGuild.bot) {
            assertPermissions(this, Permissions.MANAGE_GUILD)
            bot.request(
                RestEndpoint.CREATE_GUILD_CHANNEL.path(internalGuild.snowflake.id),
                { this@NewsChannelBuilder.apply { map = this@request as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }.await()
        }
    }
    
    override fun toString(): String = "Channel(builder)"
}

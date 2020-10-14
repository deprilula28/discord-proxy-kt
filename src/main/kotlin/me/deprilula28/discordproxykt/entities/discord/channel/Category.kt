package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.ChannelType
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PermissionOverwrite
import me.deprilula28.discordproxykt.rest.*

/**
 * an organizational category that contains up to 50 channels
 * <br>
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
interface PartialCategory: PartialEntity, PartialGuildChannel {
    companion object {
        fun new(guild: PartialGuild, id: Snowflake): PartialCategory
                = object: PartialCategory {
            override val snowflake: Snowflake = id
            override val bot: DiscordProxyKt = guild.bot
            override fun upgrade(): IRestAction<Category>
                = IRestAction.FuturesRestAction(guild.bot) {
                    guild.fetchChannels.request().thenApply {
                        it.find { ch -> ch.snowflake == id } as Category
                    }
                }
        }
    }
    
    fun upgrade(): IRestAction<Category>
}


class Category(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), GuildChannel, PartialCategory {
    override val guild: PartialGuild by map.delegateJson({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val name: String by map.delegateJson(JsonElement::asString)
    override val category: PartialCategory?
        get() = this
    override fun upgrade(): IRestAction<Category> = IRestAction.ProvidedRestAction(bot, this)
    
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map { asPermissionOverwrite(this@Category, guild, bot) }
    }, "permission_overwrites")
    
    override val type: ChannelType
        get() = ChannelType.CATEGORY
}


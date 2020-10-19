package me.deprilula28.discordproxykt.events.guild

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.guild.Guild
import me.deprilula28.discordproxykt.rest.RestEndpoint

class GuildJoinEvent(override val map: JsonObject, override val bot: DiscordProxyKt): GuildEvent {
    override val guild: Guild = Guild(map, bot)
    override val guildSnowflake: Snowflake by guild::snowflake
    
    override suspend fun internalHandle() {
        bot.cache.update(RestEndpoint.GET_GUILD.path(guildSnowflake.id), guild.map) { guild }
    }
}
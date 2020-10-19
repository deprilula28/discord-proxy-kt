package me.deprilula28.discordproxykt.events.guild.channel

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.UnavailableField
import me.deprilula28.discordproxykt.entities.discord.channel.GuildChannel
import me.deprilula28.discordproxykt.events.guild.channel.GuildChannelEvent
import me.deprilula28.discordproxykt.rest.asGuildChannel
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.parsing

class ChannelUpdateEvent(override val map: JsonObject, override val bot: DiscordProxyKt): GuildChannelEvent {
    override val guildSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "guild_id")
    override val channel: GuildChannel
        get() = map.asGuildChannel(bot, guild) ?: throw UnavailableField()
    
    override suspend fun internalHandle() {
        guild.upgrade().getIfAvailable()?.apply { (cachedChannels.find { channel.snowflake == it.snowflake } as Entity).map = map }
    }
}

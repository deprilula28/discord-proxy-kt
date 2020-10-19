package me.deprilula28.discordproxykt.events.guild.channel

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.channel.PartialGuildChannel
import me.deprilula28.discordproxykt.entities.discord.guild.PartialRole
import me.deprilula28.discordproxykt.events.guild.channel.GuildChannelEvent
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.parsing

class ChannelDeleteEvent(override val map: JsonObject, override val bot: DiscordProxyKt): GuildChannelEvent {
    override val guildSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "guild_id")
    override val channel: PartialGuildChannel by parsing({ guild.fetchChannel(asSnowflake()) }, "channel_id")
    
    override suspend fun internalHandle() {
        guild.upgrade().getIfAvailable()?.apply { cachedChannels.removeIf { it.snowflake == channel.snowflake } }
    }
}

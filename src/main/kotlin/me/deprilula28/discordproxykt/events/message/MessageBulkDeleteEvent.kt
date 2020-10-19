package me.deprilula28.discordproxykt.events.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.channel.PartialTextChannel
import me.deprilula28.discordproxykt.entities.discord.channel.TextChannel
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.events.Event
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.parsing

class MessageBulkDeleteEvent(override val map: JsonObject, override val bot: DiscordProxyKt): Event {
    val guild: PartialGuild by parsing({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    val channel: PartialTextChannel by parsing({ guild.fetchTextChannel(asSnowflake()) }, "channel_id")
    val messages: List<PartialMessage> by parsing(
        { (this as JsonArray).map { channel.fetchMessage(it.asSnowflake()) } }, "ids")
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("channel.upgrade().complete()"))
    val textChannel: TextChannel
        get() = channel.upgrade().complete()
}

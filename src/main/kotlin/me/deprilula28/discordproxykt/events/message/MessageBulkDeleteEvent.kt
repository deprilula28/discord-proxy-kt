package me.deprilula28.discordproxykt.events.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.channel.PartialTextChannel
import me.deprilula28.discordproxykt.entities.discord.channel.TextChannel
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.events.Event
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson

class MessageBulkDeleteEvent(map: JsonObject, override val bot: DiscordProxyKt): Event {
    val guild: PartialGuild by map.delegateJson({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    val channel: PartialTextChannel by map.delegateJson({ guild.fetchTextChannel(asSnowflake()) }, "channel_id")
    val messages: List<PartialMessage> by map.delegateJson({ (this as JsonArray).map { channel.fetchMessage(it.asSnowflake()) } }, "ids")
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("channel.upgrade().request().get()"))
    val textChannel: TextChannel
        get() = channel.upgrade().request().get()
}

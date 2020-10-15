package me.deprilula28.discordproxykt.events.message

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.channel.PartialMessageChannel
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson
import me.deprilula28.discordproxykt.rest.delegateJsonNullable

class MessageDeleteEvent(map: JsonObject, override val bot: DiscordProxyKt): MessageEvent {
    override val messageSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "id")
    val guild: PartialGuild? by map.delegateJsonNullable({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    val channelRaw: Snowflake by map.delegateJson(JsonElement::asSnowflake, "channel_id")
    
    override val channel: PartialMessageChannel
        get() = guild?.run { fetchTextChannel(channelRaw) } ?: bot.fetchPrivateChannel(channelRaw)
}

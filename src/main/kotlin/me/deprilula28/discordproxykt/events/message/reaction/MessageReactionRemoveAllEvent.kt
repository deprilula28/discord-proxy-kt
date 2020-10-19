package me.deprilula28.discordproxykt.events.message.reaction

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.channel.PartialMessageChannel
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.events.message.MessageEvent
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.parsing
import me.deprilula28.discordproxykt.rest.parsingOpt

class MessageReactionRemoveAllEvent(override val map: JsonObject, override val bot: DiscordProxyKt): MessageEvent {
    val channelRaw: Snowflake by parsing(JsonElement::asSnowflake, "channel_id")
    override val channel: PartialMessageChannel
        get() = guild?.run { fetchTextChannel(channelRaw) } ?: bot.fetchPrivateChannel(channelRaw)
    override val messageSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "message_id")
    
    val guild: PartialGuild? by parsingOpt({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    val message: PartialMessage by parsing({ channel.fetchMessage(messageSnowflake) }, "message_id")
}

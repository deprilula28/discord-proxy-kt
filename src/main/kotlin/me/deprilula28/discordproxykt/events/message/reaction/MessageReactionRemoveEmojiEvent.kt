package me.deprilula28.discordproxykt.events.message.reaction

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.channel.PartialMessageChannel
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.entities.discord.message.ReactionEmoji
import me.deprilula28.discordproxykt.events.message.MessageEvent
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson
import me.deprilula28.discordproxykt.rest.delegateJsonNullable

class MessageReactionRemoveEmojiEvent(map: JsonObject, override val bot: DiscordProxyKt): MessageEvent {
    val channelRaw: Snowflake by map.delegateJson(JsonElement::asSnowflake, "channel_id")
    override val channel: PartialMessageChannel
        get() = guild?.run { fetchTextChannel(channelRaw) } ?: bot.fetchPrivateChannel(channelRaw)
    override val messageSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "message_id")
    
    val guild: PartialGuild? by map.delegateJsonNullable({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    val message: PartialMessage by map.delegateJson({ channel.fetchMessage(messageSnowflake) }, "message_id")
    
    val emoji: ReactionEmoji by map.delegateJson({ ReactionEmoji(this as JsonObject, bot) })
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("this"))
    val reaction: MessageReactionRemoveEmojiEvent
        get() = this
    @Deprecated("JDA Compatibility Field", ReplaceWith("emoji"))
    val reactionEmote: ReactionEmoji
        get() = emoji
}

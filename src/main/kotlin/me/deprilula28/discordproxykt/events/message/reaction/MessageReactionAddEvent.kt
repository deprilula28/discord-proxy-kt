package me.deprilula28.discordproxykt.events.message.reaction

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.Member
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.entities.discord.channel.PartialMessageChannel
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.entities.discord.message.ReactionEmoji
import me.deprilula28.discordproxykt.rest.IRestAction
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson
import me.deprilula28.discordproxykt.rest.delegateJsonNullable

class MessageReactionAddEvent(map: JsonObject, override val bot: DiscordProxyKt): MessageReactionEvent {
    override val user: PartialUser by map.delegateJson({ bot.fetchUser(asSnowflake()) }, "user_id")
    override val guild: PartialGuild? by map.delegateJsonNullable({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    val channelRaw: Snowflake by map.delegateJson(JsonElement::asSnowflake, "channel_id")
    override val channel: PartialMessageChannel
        get() = guild?.run { fetchTextChannel(channelRaw) } ?: bot.fetchPrivateChannel(channelRaw)
    override val snowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "message_id")
    override val message: PartialMessage by map.delegateJson({ channel.fetchMessage(snowflake) }, "message_id")
    override val member: Member? by map.delegateJson({ Member(guild!!, this as JsonObject, bot) })
    override val emoji: ReactionEmoji by map.delegateJson({ ReactionEmoji(this as JsonObject, bot) })
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("IRestAction.ProvidedRestAction(bot, member!!"))
    fun retrieveMember() = IRestAction.ProvidedRestAction(bot, member!!)
}

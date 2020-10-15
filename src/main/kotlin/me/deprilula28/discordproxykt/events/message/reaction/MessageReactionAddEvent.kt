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
import me.deprilula28.discordproxykt.rest.parsing
import me.deprilula28.discordproxykt.rest.parsingOpt

class MessageReactionAddEvent(override val map: JsonObject, override val bot: DiscordProxyKt): MessageReactionEvent {
    override val user: PartialUser by parsing({ bot.fetchUser(asSnowflake()) }, "user_id")
    override val guild: PartialGuild? by parsingOpt({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    val channelRaw: Snowflake by parsing(JsonElement::asSnowflake, "channel_id")
    override val channel: PartialMessageChannel
        get() = guild?.run { fetchTextChannel(channelRaw) } ?: bot.fetchPrivateChannel(channelRaw)
    override val messageSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "message_id")
    override val message: PartialMessage by parsing({ channel.fetchMessage(asSnowflake()) }, "message_id")
    override val member: Member? by parsing({ Member(guild!!, this as JsonObject, bot) })
    override val emoji: ReactionEmoji by parsing({ ReactionEmoji(this as JsonObject, bot) })
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("IRestAction.ProvidedRestAction(bot, member!!"))
    fun retrieveMember() = IRestAction.ProvidedRestAction(bot, member!!)
}

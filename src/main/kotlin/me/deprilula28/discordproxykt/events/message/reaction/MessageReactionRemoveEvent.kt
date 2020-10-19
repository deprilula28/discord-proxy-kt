package me.deprilula28.discordproxykt.events.message.reaction

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.UnavailableField
import me.deprilula28.discordproxykt.entities.discord.guild.Member
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.entities.discord.channel.PartialMessageChannel
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.entities.discord.message.ReactionEmoji
import me.deprilula28.discordproxykt.rest.IRestAction
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.parsing
import me.deprilula28.discordproxykt.rest.parsingOpt

class MessageReactionRemoveEvent(override val map: JsonObject, override val bot: DiscordProxyKt): MessageReactionEvent {
    override val user: PartialUser by parsing({ bot.fetchUser(asSnowflake()) }, "user_id")
    override val guild: PartialGuild? by parsingOpt({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    val channelRaw: Snowflake by parsing(JsonElement::asSnowflake, "channel_id")
    override val channel: PartialMessageChannel
        get() = guild?.run { fetchTextChannel(channelRaw) } ?: bot.fetchPrivateChannel(channelRaw)
    override val messageSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "message_id")
    override val message: PartialMessage by parsing({ channel.fetchMessage(messageSnowflake) }, "message_id")
    override val member: Member? = null
    override val emoji: ReactionEmoji? by parsingOpt({
        val obj = this as JsonObject
        if (obj["id"] == null || obj["id"] is JsonNull) null
        else ReactionEmoji(obj, bot)
    })
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("(guild ?: throw UnavailableField()).upgrade().map { it.fetchMember(user.snowflake) }"))
    fun retrieveMember() = IRestAction.coroutine(bot) {
        (guild ?: throw UnavailableField()).upgrade().await().fetchMember(user.snowflake)
    }
}

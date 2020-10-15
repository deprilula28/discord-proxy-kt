package me.deprilula28.discordproxykt.events.message.reaction

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.UnavailableField
import me.deprilula28.discordproxykt.entities.discord.Member
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.entities.discord.channel.PartialMessageChannel
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.entities.discord.message.ReactionEmoji
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson
import me.deprilula28.discordproxykt.rest.delegateJsonNullable

class MessageReactionRemoveEvent(map: JsonObject, override val bot: DiscordProxyKt): MessageReactionEvent {
    override val user: PartialUser by map.delegateJson({ bot.fetchUser(asSnowflake()) }, "user_id")
    override val guild: PartialGuild? by map.delegateJsonNullable({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    val channelRaw: Snowflake by map.delegateJson(JsonElement::asSnowflake, "channel_id")
    override val channel: PartialMessageChannel
        get() = guild?.run { fetchTextChannel(channelRaw) } ?: bot.fetchPrivateChannel(channelRaw)
    override val messageSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "message_id")
    override val message: PartialMessage by map.delegateJson({ channel.fetchMessage(messageSnowflake) }, "message_id")
    override val member: Member? = null
    override val emoji: ReactionEmoji? by map.delegateJsonNullable({
        val obj = this as JsonObject
        if (obj["id"] == null || obj["id"] is JsonNull) null
        else ReactionEmoji(obj, bot)
    })
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("(guild ?: throw UnavailableField()).upgrade().map { it.fetchMember(user.snowflake) }"))
    fun retrieveMember() = (guild ?: throw UnavailableField()).upgrade().map { it.fetchMember(user.snowflake) }
}

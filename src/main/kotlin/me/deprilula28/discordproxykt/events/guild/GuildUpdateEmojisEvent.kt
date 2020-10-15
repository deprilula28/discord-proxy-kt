package me.deprilula28.discordproxykt.events.guild

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.message.GuildEmoji
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.parsing

class GuildUpdateEmojisEvent(override val map: JsonObject, override val bot: DiscordProxyKt): GuildEvent {
    override val guildSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "guild_id")
    val emojis: List<GuildEmoji> by parsing({ (this as JsonArray).map { GuildEmoji(it as JsonObject, bot) } })
}
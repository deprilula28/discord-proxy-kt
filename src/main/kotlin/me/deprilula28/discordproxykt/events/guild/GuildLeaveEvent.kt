package me.deprilula28.discordproxykt.events.guild

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.Guild
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson

class GuildLeaveEvent(map: JsonObject, override val bot: DiscordProxyKt): GuildEvent {
    override val snowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "id")
}
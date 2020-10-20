package me.deprilula28.discordproxykt.events.guild

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.guild.Member
import me.deprilula28.discordproxykt.rest.asInt
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.asString
import me.deprilula28.discordproxykt.rest.parsing

class GuildMembersChunk(override val map: JsonObject, override val bot: DiscordProxyKt): GuildEvent {
    override val guildSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "guild_id")
    val members by parsing({ (this as JsonArray).map { Member(guild, it as JsonObject, bot) } })
    val chunkIndex by parsing(JsonElement::asInt, "chunk_index")
    val chunkCount by parsing(JsonElement::asInt, "chunk_count")
    val notFound by parsing({ (this as JsonArray).map(JsonElement::asString) }, "not_found")
    val nonce by parsing(JsonElement::asString)
}

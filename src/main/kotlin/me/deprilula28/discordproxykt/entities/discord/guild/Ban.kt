package me.deprilula28.discordproxykt.entities.discord.guild

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Parse
import me.deprilula28.discordproxykt.entities.discord.User
import me.deprilula28.discordproxykt.rest.asString
import me.deprilula28.discordproxykt.rest.parsing

/**
 * https://discord.com/developers/docs/resources/guild#ban-object
 */
data class Ban(override val map: JsonObject, override val bot: DiscordProxyKt): Parse {
    val user: User by parsing({ User(this as JsonObject, bot) })
    val reason: String by parsing(JsonElement::asString)
}
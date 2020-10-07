package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.asBoolean
import me.deprilula28.discordproxykt.entities.asInt
import me.deprilula28.discordproxykt.entities.delegateJson

// https://discord.com/developers/docs/resources/channel#reaction-object
class Reaction(private val map: JsonObject, val bot: DiscordProxyKt) {
    val count: Int by map.delegateJson(JsonElement::asInt)
    val me: Boolean by map.delegateJson(JsonElement::asBoolean)
    val emoji: Emoji by map.delegateJson({
        val obj = this as JsonObject
        val id = obj["id"]
        if (id == null || id == JsonNull || id.toString() == "null") UnicodeEmoji(obj) else ReactionEmoji(obj, bot)
    })
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot")) val jda: DiscordProxyKt by ::bot
}

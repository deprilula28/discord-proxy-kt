package me.deprilula28.discordproxykt.entities.discord.guild

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.rest.asBoolean
import me.deprilula28.discordproxykt.rest.asString
import me.deprilula28.discordproxykt.rest.parsing

/**
 * https://discord.com/developers/docs/resources/voice#voice-region-object
 */
class VoiceRegion(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    /**
     * name of the region
     */
    val name: String by parsing(JsonElement::asString)
    /**
     * true if this is a vip-only server
     * <br>
     * "VIP" regions are now known as 368kbps regions for boosted servers
     */
    val vip: Boolean by parsing(JsonElement::asBoolean)
    /**
     * true for a single server that is closest to the current user's client
     */
    val optimal: Boolean by parsing(JsonElement::asBoolean)
    /**
     * whether this is a deprecated voice region (avoid switching to these)
     */
    val deprecated: Boolean by parsing(JsonElement::asBoolean)
    /**
     * whether this is a custom voice region (used for events/etc)
     */
    val custom: Boolean by parsing(JsonElement::asBoolean)
}
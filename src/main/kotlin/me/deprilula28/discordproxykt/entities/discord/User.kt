package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import java.util.*

// https://discord.com/developers/docs/resources/user#user-object
class User(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    val username: String by map.delegateJson(JsonElement::asString)
    val discriminator: String by map.delegateJson(JsonElement::asString)
    val avatarHash: String? by map.delegateJsonNullable(JsonElement::asString, "avatar")
    val isBot: Boolean by map.delegateJson(JsonElement::asBoolean, "bot")
    val system: Boolean by map.delegateJson(JsonElement::asBoolean)
    val publicFlags: EnumSet<Flags> by lazy { map["public_flags"]!!.asLong().bitSetToEnumSet(Flags.values()) }
    
    // Logged on users through OAuth2 only
    val flags: EnumSet<Flags>? by map.delegateJsonNullable({ asLong().bitSetToEnumSet(Flags.values()) })
    val locale: String? by map.delegateJsonNullable(JsonElement::asString)
    val verified: Boolean? by map.delegateJsonNullable(JsonElement::asBoolean)
    val email: String? by map.delegateJsonNullable(JsonElement::asString)
    val multipleFactor: Boolean? by map.delegateJsonNullable(JsonElement::asBoolean)
    val premiumType: PremiumType? by map.delegateJsonNullable({ PremiumType.values()[asInt()] }, "premium_type")
    
    enum class PremiumType {
        NONE, CLASSIC, NITRO
    }
    
    enum class Flags {
        EMPLOYEE,
        PARTNER,
        HYPESQUAD,
        BUG_HUNTER,
        EMPTY_00,
        EMPTY_01,
        BRAVERY,
        BRILLIANCE,
        BALANCE,
        EARLY_SUPPORTER,
        STAFF,
        EMPTY_02,
        SYSTEM,
        EMPTY_03,
        BUG_HUNTER_LVL_2,
        EMPTY_04,
        VERIFIED_BOT,
        VERIFIED_EARLY_BOT_DEVELOPER,
    }
}

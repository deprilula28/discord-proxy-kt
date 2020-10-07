package me.deprilula28.discordproxykt.entities.discord

import me.deprilula28.discordproxykt.JdaProxySpectacles
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.*
import java.util.*

// https://discord.com/developers/docs/resources/user#user-object
class User(map: JsonObject, bot: JdaProxySpectacles): Entity(map, bot) {
    val username: String by lazy { map["username"]!!.asString() }
    val discriminator: String by lazy { map["discriminator"]!!.asString() }
    val avatarHash: String? by lazy { map["avatar"]?.asString() }
    val isBot: Boolean by lazy { map["bot"]!!.asBoolean() }
    val system: Boolean by lazy { map["system"]!!.asBoolean() }
    val publicFlags: EnumSet<Flags> by lazy { map["flags"]!!.asLong().bitSetToEnumSet(Flags.values()) }
    
    // Logged on users through OAuth2 only
    val flags: EnumSet<Flags>? by lazy { map["flags"]?.asLong()?.bitSetToEnumSet(Flags.values()) }
    val locale: String? by lazy { map["locale"]?.asString() }
    val verified: Boolean? by lazy { map["verified"]?.asBoolean() }
    val email: String? by lazy { map["email"]?.asString() }
    val multipleFactor: Boolean? by lazy { map["mfa"]?.asBoolean() }
    val premiumType: PremiumType? by lazy { map["premium_type"]?.asInt()?.run { PremiumType.values()[this] } }
    
    enum class PremiumType {
        NONE,
        CLASSIC,
        NITRO
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

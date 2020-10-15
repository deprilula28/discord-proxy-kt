package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.rest.*
import java.util.*

// TODO Other methods
interface PartialUser: PartialEntity, Message.Mentionable {
    companion object {
        fun new(id: Snowflake, bot: DiscordProxyKt): PartialUser
            = object: PartialUser {
                override val snowflake: Snowflake = id
                override val bot: DiscordProxyKt = bot
                override fun upgrade(): IRestAction<User>
                    = RestAction(
                        bot, RestEndpoint.GET_USER.path(id.id),
                        { User(this as JsonObject, bot) }
                    )
            }
    }
    
    fun upgrade(): IRestAction<User>
    override val asMention: String
        get() = "<@${snowflake.id}>"
}

/**
 * https://discord.com/developers/docs/resources/user#user-object
 */

class User(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), PartialUser {
    /**
     * From https://discord.com/developers/docs/resources/user#usernames-and-nicknames
     * Discord enforces the following restrictions for usernames and nicknames:
     * - Names can contain most valid unicode characters. We limit some zero-width and non-rendering characters.
     * - Usernames must be between 2 and 32 characters long.
     * - Nicknames must be between 1 and 32 characters long.
     * - Names are sanitized and trimmed of leading, trailing, and excessive internal whitespace.
     * The following restrictions are additionally enforced for usernames:
     * - Names cannot contain the following substrings: '@', '#', ':', '```'.
     * - Names cannot be: 'discordtag', 'everyone', 'here'.
     * There are other rules and restrictions not shared here for the sake of spam and abuse mitigation,
     * but the majority of users won't encounter them. It's important to properly handle all error messages returned by
     * Discord when editing or updating names.
     * <br>
     * 
     */
    val username: String by parsing(JsonElement::asString)
    /**
     * the user's 4-digit discord-tag
     */
    val discriminator: String by parsing(JsonElement::asString)
    /**
     * whether the user belongs to an OAuth2 application
     */
    val isBot: Boolean by lazy { map["bot"]?.asBoolean() ?: false }
    /**
     * whether the user is an Official Discord System user (part of the urgent message system)
     */
    val system: Boolean by lazy { map["system"]?.asBoolean() ?: false }
    
    /**
     * the user's avatar hash
     */
    val avatarHash: String? by parsingOpt(JsonElement::asString, "avatar")
    /**
     * the public flags on a user's account
     */
    val publicFlags: EnumSet<Flags> by lazy { map["public_flags"]!!.asLong().bitSetToEnumSet(Flags.values()) }
    
    /**
     * the flags on a user's account
     * <br>
     * Logged on users through OAuth2 only
     */
    val flags: EnumSet<Flags>? by parsingOpt({ asLong().bitSetToEnumSet(Flags.values()) })
    /**
     * the user's chosen language option
     * <br>
     * Logged on users through OAuth2 only
     */
    val locale: String? by parsingOpt(JsonElement::asString)
    /**
     * whether the email on this account has been verified
     * <br>
     * Logged on users through OAuth2 only with `email` intent
     */
    val verified: Boolean? by parsingOpt(JsonElement::asBoolean)
    /**
     * the user's email
     * <br>
     * Logged on users through OAuth2 only with `email` intent
     */
    val email: String? by parsingOpt(JsonElement::asString)
    /**
     * whether the user has two factor enabled on their account
     * <br>
     * Logged on users through OAuth2 only
     */
    val multipleFactor: Boolean? by parsingOpt(JsonElement::asBoolean, "mfa")
    /**
     * the type of Nitro subscription on a user's account
     * <br>
     * Logged on users through OAuth2 only
     */
    val premiumType: PremiumType? by parsingOpt({ PremiumType.values()[asInt()] }, "premium_type")
    
    override fun upgrade(): IRestAction<User> = IRestAction.ProvidedRestAction(bot, this)
    
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

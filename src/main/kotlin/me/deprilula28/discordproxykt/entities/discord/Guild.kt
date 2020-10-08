package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.message.GuildEmoji
import me.deprilula28.discordproxykt.rest.IRestAction
import java.util.*

interface PartialGuild: IPartialEntity {
    interface Upgradeable: PartialUser, IRestAction<Guild>
}

/**
 * Guilds in Discord represent an isolated collection of users and channels, and are often referred to as "servers" in the UI.
 */
class Guild(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), PartialUser {
    /**
     * guild name (2-100 characters, excluding trailing and leading whitespace)
     */
    val name: String by map.delegateJson(JsonElement::asString)
    /**
     * id of owner
     */
    val ownerSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake)
    /**
     * application id of the guild creator if it is bot-created
     */
    val applicationSnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "application_id")
    
    /**
     * roles in the guild
     */
    val roles: List<Role> by map.delegateJson({ (this as JsonArray).map { Role(it as JsonObject, bot) } })
    /**
     * custom guild emojis
     */
    val emojis: List<GuildEmoji> by map.delegateJson({ (this as JsonArray).map { GuildEmoji(it as JsonObject, bot) } })
    
    /**
     * the description for the guild, if the guild is discoverable
     */
    val description: String? by map.delegateJsonNullable(JsonElement::asString)
    /**
     * discovery splash hash; only present for guilds with the "DISCOVERABLE" feature
     */
    val discoverableSplash: String? by map.delegateJsonNullable(JsonElement::asString, "discovery_splash")
    /**
     * banner hash
     */
    val banner: String? by map.delegateJsonNullable(JsonElement::asString)
    /**
     * the id of the channel where guilds with the "PUBLIC" feature can display rules and/or guidelines
     */
    val rulesChannelSnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "rules_channel_id")
    /**
     * 	the preferred locale of a guild with the "PUBLIC" feature; used in server discovery and notices from Discord; defaults to "en-US"
     */
    val preferredLocale: String by map.delegateJson(JsonElement::asString, "preferred_locale")
    /**
     * 	the id of the channel where admins and moderators of guilds with the "PUBLIC" feature receive notices from Discord
     */
    val publicUpdatesChannelSnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "public_updates_channel_id")
    
    /**
     * icon hash
     */
    val icon: String? by map.delegateJsonNullable(JsonElement::asString)
    /**
     * splash hash
     */
    val iconSplash: String? by map.delegateJsonNullable(JsonElement::asString, "splash")
    
    /**
     * voice region for the guild
     */
    val region: String by map.delegateJson(JsonElement::asString)
    /**
     * id of afk channel
     */
    val afkChannelSnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "afk_channel_id")
    /**
     * afk timeout in seconds
     */
    val afkTimeout: Int by map.delegateJson(JsonElement::asInt, "afk_timeout")
    
    /**
     * true if the server widget is enabled
     */
    val widgetEnabled: Boolean? by map.delegateJsonNullable(JsonElement::asBoolean, "widget_enabled")
    /**
     * the channel id that the widget will generate an invite to, or null if set to no invite
     */
    val widgetChannelSnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "widget_channel_id")
    
    /**
     * true if the user is the owner of the guild
     * <br>
     * Only sent under "GET Current User Guilds" endpoint, relative to current user
     */
    val userIsOwner: Boolean? by map.delegateJsonNullable(JsonElement::asBoolean, "owner")
    /**
     * total permissions for the user in the guild (excludes overrides)
     * <br>
     * Only sent under "GET Current User Guilds" endpoint, relative to current user
     */
    val userPermissions: EnumSet<Permissions>? by map.delegateJsonNullable({ asLong().bitSetToEnumSet(Permissions.values()) }, "permissions")
    
    /**
     * when this guild was joined at
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val joinedAt: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "joined_at")
    /**
     * true if this is considered a large guild
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val large: Boolean? by map.delegateJsonNullable(JsonElement::asBoolean)
    /**
     * true if this guild is unavailable due to an outage
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val unavailable: Boolean? by map.delegateJsonNullable(JsonElement::asBoolean)
    /**
     * total number of members in this guild
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val memberCount: Int? by map.delegateJsonNullable(JsonElement::asInt, "member_count")
    /**
     * 	approximate number of members in this guild
     */
    val approxMemberCount: Int? by map.delegateJsonNullable(JsonElement::asInt, "approximate_member_count")
    /**
     * 	approximate number of non-offline members in this guild
     */
    val approxPresenceCount: Int? by map.delegateJsonNullable(JsonElement::asInt, "approximate_presence_count")
    /**
     * users in the guild
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val members: List<Member>? by map.delegateJsonNullable({ (this as JsonArray).map { Member(it as JsonObject, bot) } })
    /**
     * channels in the guild
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val channels: List<GuildChannel>? by map.delegateJsonNullable({
        (this as JsonArray).map {
            val obj = it as JsonObject
            val type = obj["type"]!!.asInt()
            when (type) {
                0 -> TextChannel(obj, bot)
                2 -> VoiceChannel(obj, bot)
                4 -> Category(obj, bot)
                else -> {
                    println("Invalid channel type received for guild ${snowflake.id}: $type")
                    null
                }
            }
        }.filterNotNull()
    })
    
    /**
     * the maximum number of presences for the guild (the default value, currently 25000, is in effect when null is returned)
     */
    val maxPresences: Int? by map.delegateJsonNullable(JsonElement::asInt, "max_presences")
    /**
     * the maximum number of members for the guild
     */
    val maxMembers: Int? by map.delegateJsonNullable(JsonElement::asInt, "max_members")
    /**
     * the maximum amount of users in a video channel
     */
    val maxVideoChannelUsers: Int? by map.delegateJsonNullable(JsonElement::asInt, "max_video_channel_users")
    /**
     * the vanity url code for the guild
     */
    val vanityUrlCode: String? by map.delegateJsonNullable(JsonElement::asString, "vanity_url_code")
    
    /**
     * verification level required for the guild
     */
    val verificationLevel: VerificationLevel by map.delegateJson({ VerificationLevel.values()[asInt()] }, "verification_level")
    
    /**
     * https://discord.com/developers/docs/resources/guild#guild-object-verification-level
     */
    enum class VerificationLevel {
        /**
         * Unrestricted
         */
        NONE,
        /**
         * Must have verified email on account
         */
        LOW,
        /**
         * Must be registered on Discord for longer than 5 minutes
         */
        MEDIUM,
        /**
         * (╯°□°）╯︵ ┻━┻ - must be a member of the server for longer than 10 minutes
         */
        HIGH,
        /**
         * ┻━┻ ミヽ(ಠ 益 ಠ)ﾉ彡 ┻━┻ - must have a verified phone number
         */
        VERY_HIGH
    }
    
    /**
     * default message notifications level
     */
    val defaultNotificationLevel: NotificationLevel by map.delegateJson({ NotificationLevel.values()[asInt()] }, "default_message_notifications")
    
    /**
     * https://discord.com/developers/docs/resources/guild#guild-object-default-message-notification-level
     */
    enum class NotificationLevel {
        ALL_MESSAGES,
        MENTIONS_ONLY
    }
    
    /**
     * explicit content filter level
     */
    val explicitContentLevel: ExplicitContentFilterLevel by map.delegateJson({ ExplicitContentFilterLevel.values()[asInt()] }, "explicit_content_filter")
    
    /**
     * https://discord.com/developers/docs/resources/guild#guild-object-explicit-content-filter-level
     */
    enum class ExplicitContentFilterLevel {
        OFF,
        NO_ROLE,
        ALL,
    }
    
    // This is encoded as a list of strings instead of a bitset for god knows why
    /**
     * enabled guild features
     */
    val features: EnumSet<Features> by map.delegateJson({
        val set = EnumSet.noneOf(Features::class.java)
        (this as JsonArray).forEach { set.add(Features.valueOf(it.asString())) }
        set
    })
    /**
     * https://discord.com/developers/docs/resources/guild#guild-object-guild-features
     */
    enum class Features {
        /**
         * Guild has access to set an invite splash background
         */
        INVITE_SPLASH,
        /**
         * Guild has access to set 384kbps bitrate in voice (previously VIP voice servers)
         */
        VIP_REGIONS,
        /**
         * Guild has access to set a vanity URL
         */
        VANITY_URL,
        /**
         * Guild is verified
         */
        VERIFIED,
        /**
         * Guild is partnered
         */
        PARTNERED,
        /**
         * Guild is public
         */
        PUBLIC,
        /**
         * Guild has access to use commerce features (i.e. create store channels)
         */
        COMMERCE,
        /**
         * Guild has access to create news channels
         */
        NEWS,
        /**
         * Guild is able to be discovered in the directory
         */
        DISCOVERABLE,
        /**
         * Guild is able to be featured in the directory
         */
        FEATURABLE,
        /**
         * Guild has access to set an animated guild icon
         */
        ANIMATED_ICON,
        /**
         * Guild has access to set a guild banner image
         */
        BANNER,
        /**
         * Guild cannot be public
         */
        PUBLIC_DISABLED,
        /**
         * Guild has enabled the welcome screen
         */
        WELCOME_SCREEN_ENABLED,
    }
    
    /**
     * required MFA level for the guild
     */
    val requiredMFALevel: MFALevel by map.delegateJson({ MFALevel.values()[asInt()] }, "mfa_level")
    
    /**
     * https://discord.com/developers/docs/resources/guild#guild-object-mfa-level
     */
    enum class MFALevel {
        NONE,
        ELEVATED
    }
    
    /**
     * premium tier (Server Boost level)
     */
    val premiumTier: PremiumTier by map.delegateJson({ PremiumTier.values()[asInt()] }, "premium_tier")
    /**
     * the number of boosts this guild currently has
     */
    val boosters: Int? by map.delegateJsonNullable(JsonElement::asInt, "premium_subscription_count")
    
    /**
     * https://discord.com/developers/docs/resources/guild#guild-object-premium-tier
     */
    enum class PremiumTier {
        NONE,
        TIER_1,
        TIER_2,
        TIER_3,
    }
    
    /**
     * the id of the channel where guild notices such as welcome messages and boost events are posted
     */
    val systemChannelSnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "system_channel_id")
    /**
     * system channel flags
     */
    val systemChannelFlags: EnumSet<SystemChannelFlags> by map.delegateJson({ asLong().bitSetToEnumSet(SystemChannelFlags.values()) }, "system_channel_flags")
    
    /**
     * https://discord.com/developers/docs/resources/guild#guild-object-system-channel-flags
     */
    enum class SystemChannelFlags {
        /**
         * Suppress member join notifications
         */
        SUPPRESS_JOIN_NOTIFICATIONS,
        /**
         * Suppress server boost notifications
         */
        SUPPRESS_PREMIUM_SUBSCRIPTIONS,
    }
}
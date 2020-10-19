package me.deprilula28.discordproxykt.entities.discord.guild

enum class Region {
    AMSTERDAN, VIP_AMSTERDAN,
    BRAZIL, VIP_BRAZIL,
    EUROPE,  VIP_EUROPE,
    EU_CENTRAL,  VIP_EU_CENTRAL,
    EU_WEST, VIP_EU_WEST,
    FRANKFURT,  VIP_FRANKFURT,
    HONG_KONG,  VIP_HONG_KONG,
    JAPAN, VIP_JAPAN,
    SOUTH_KOREA, VIP_SOUTH_KOREA,
    LONDON, VIP_LONDON,
    RUSSIA,  VIP_RUSSIA,
    INDIA,  VIP_INDIA,
    SINGAPORE, VIP_SINGAPORE,
    SOUTH_AFRICA, VIP_SOUTH_AFRICA,
    SYDNEY, VIP_SYDNEY,
    US_CENTRAL, VIP_US_CENTRAL,
    US_EAST, VIP_US_EAST,
    US_SOUTH, VIP_US_SOUTH,
    US_WEST, VIP_US_WEST,
}

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-mfa-level
 */
enum class MFALevel {
    NONE,
    ELEVATED
}

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-explicit-content-filter-level
 */
enum class ExplicitContentFilterLevel {
    OFF,
    NO_ROLE,
    ALL,
}

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-default-message-notification-level
 */
enum class NotificationLevel {
    ALL_MESSAGES,
    MENTIONS_ONLY
}

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

enum class Timeout(val seconds: Int) {
    SECONDS_60(60),
    SECONDS_300(300),
    SECONDS_900(900),
    SECONDS_1800(1800),
    SECONDS_3600(3600);
}

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
    /**
     * Community server
     */
    COMMUNITY,
}

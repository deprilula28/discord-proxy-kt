package me.deprilula28.discordproxykt.entities.discord.guild

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
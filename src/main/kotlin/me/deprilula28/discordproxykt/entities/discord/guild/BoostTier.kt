package me.deprilula28.discordproxykt.entities.discord.guild

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-premium-tier
 */
enum class BoostTier(val bitrate: Int, val emotes: Int, val fileSize: Long) {
    NONE(96000, 50, 8_388_608L),
    TIER_1(128000, 100, 8_388_608L),
    TIER_2(256000, 150, 52_428_800L),
    TIER_3(384000, 250, 104_857_600L),
}
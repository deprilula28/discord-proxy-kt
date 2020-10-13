package me.deprilula28.discordproxykt.entities.discord.channel

import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.discord.ChannelType
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PermissionOverwrite

interface PartialGuildChannel: PartialEntity {
    interface Upgradeable: PartialGuildChannel
}

/**
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
interface GuildChannel: PartialGuildChannel {
    /**
     * the id of the guild
     */
    val guild: PartialGuild.Upgradeable
    /**
     * sorting position of the channel
     */
    val position: Int
    /**
     * the name of the channel (2-100 characters)
     */
    val name: String
    /**
     * explicit permission overwrites for members and roles
     */
    val permissions: List<PermissionOverwrite>
    /**
     * id of the parent category for a channel (each parent category can contain up to 50 channels)
     */
    val category: PartialCategory?
    /**
     * Channel Type, should be constant
     */
    val type: ChannelType
}
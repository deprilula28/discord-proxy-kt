package entities.discord

import JdaProxySpectacles
import entities.Entity
import entities.Snowflake

class GuildChannel(
    val id: Snowflake,
    val guildId: Snowflake,
    val position: Int,
    val name: String,
    val permissions: List<PermissionOverwrite>,
    bot: JdaProxySpectacles,
): Entity(bot)

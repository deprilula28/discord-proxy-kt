package entities.discord

import JdaProxySpectacles
import entities.Entity
import entities.Snowflake
import kotlinx.serialization.Serializable

class MessageChannel(
    val id: Snowflake,
    val lastMessageId: Snowflake,
    val rateLimitPerUser: Int?,
    bot: JdaProxySpectacles,
): Entity(bot)

package events.message

import JdaProxySpectacles
import entities.Snowflake
import events.GenericEvent

open class GenericMessageEvent(
    val id: Snowflake,
    val channelId: Snowflake,
    val guildId: Snowflake?,
    bot: JdaProxySpectacles,
): GenericEvent(bot)

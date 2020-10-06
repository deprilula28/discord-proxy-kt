package entities.discord

import JdaProxySpectacles
import entities.Entity

class TextChannel(
    val guildChannel: GuildChannel,
    val messageChannel: MessageChannel,
    val topic: String,
    val nsfw: Boolean,
    bot: JdaProxySpectacles,
): Entity(bot)

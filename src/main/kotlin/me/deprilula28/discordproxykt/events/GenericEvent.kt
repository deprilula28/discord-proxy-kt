package events

import me.deprilula28.discordproxykt.JdaProxySpectacles

open class GenericEvent(val bot: JdaProxySpectacles) {
    val jda: JdaProxySpectacles
        get() = bot
}
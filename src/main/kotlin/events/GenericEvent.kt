package events

import JdaProxySpectacles

open class GenericEvent(val bot: JdaProxySpectacles) {
    val jda: JdaProxySpectacles
        get() = bot
}
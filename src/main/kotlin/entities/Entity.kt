package entities

import JdaProxySpectacles

open class Entity(val bot: JdaProxySpectacles) {
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot"))
    val jda: JdaProxySpectacles
        get() = bot
}
package me.deprilula28.discordproxykt.cache

import me.deprilula28.discordproxykt.rest.RestEndpoint

class NoCache: DiscordRestCache {
    override suspend fun store(key: RestEndpoint.Path, type: DiscordRestCache.StoreType, value: Any): Boolean = false
    override suspend fun <T: Any> retrieve(key: RestEndpoint.Path): T? = null
}
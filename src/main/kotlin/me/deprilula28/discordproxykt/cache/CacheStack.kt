package me.deprilula28.discordproxykt.cache

import me.deprilula28.discordproxykt.rest.RestEndpoint

open class CacheStack(private val layers: List<DiscordRestCache>): DiscordRestCache {
    // Store in first layer willing to accept it or return false
    override suspend fun store(key: RestEndpoint.Path, type: DiscordRestCache.StoreType, value: Any): Boolean {
        for (layer in layers)
            if (layer.store(key, type, value)) return true
        return false
    }
    
    // Retrieve from first layer with the data or return null
    override suspend fun <T: Any> retrieve(key: RestEndpoint.Path): T? {
        for (layer in layers)
            layer.retrieve<T>(key)?.run { return@retrieve this }
        return null
    }
}

package me.deprilula28.discordproxykt.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.deprilula28.discordproxykt.rest.InvalidRequestException
import me.deprilula28.discordproxykt.rest.RestEndpoint
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MemoryCache(val scope: CoroutineScope = GlobalScope, retention: Pair<Long, TimeUnit>): DiscordRestCache {
    private val map = ConcurrentHashMap<Any, Any>()
    private val retentionMillis = retention.second.toMillis(retention.first)
    
    override suspend fun store(key: RestEndpoint.Path, type: DiscordRestCache.StoreType, value: Any): Boolean {
        if (type == DiscordRestCache.StoreType.PASSIVE) return false
        map[key] = value
        scope.launch {
            delay(retentionMillis)
            map.remove(key)
        }
        return true
    }
    
    @Suppress("UNCHECKED_CAST") override suspend fun <T: Any> retrieve(key: RestEndpoint.Path): T?
        = map[key]?.run { this as? T ?: throw InvalidRequestException("Request has cache of unexpected type ${this::class.java}: $this") }
}

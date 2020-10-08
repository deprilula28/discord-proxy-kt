package me.deprilula28.discordproxykt.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MemoryCache(val scope: CoroutineScope = GlobalScope, retention: Pair<Long, TimeUnit>): Cache {
    private val map = ConcurrentHashMap<Any, Any>()
    private val retentionMillis = retention.second.toMillis(retention.first)
    
    override fun store(key: Any, value: Any): Boolean {
        map[key] = value
        scope.launch {
            delay(retentionMillis)
            map.remove(key)
        }
        return true
    }
    
    @Suppress("UNCHECKED_CAST") override fun <T: Any> retrieve(key: Any): T? = map[key]?.run { this as T }
}
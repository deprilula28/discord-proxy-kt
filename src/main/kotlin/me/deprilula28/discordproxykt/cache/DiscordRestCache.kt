package me.deprilula28.discordproxykt.cache

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.rest.RestEndpoint

interface DiscordRestCache {
    enum class StoreType {
        /**
         * This value was the response from the Discord API upon a request
         */
        REQUEST,
        /**
         * This value was received from an event and can be cached from this point
         */
        PASSIVE,
    }
    
    suspend fun store(key: RestEndpoint.Path, type: StoreType, value: Any): Boolean
    suspend fun <T: Any> retrieve(key: RestEndpoint.Path): T?
    
    suspend fun <T: Any> get(key: RestEndpoint.Path, type: StoreType, fallback: () -> T): T {
        val retrieved = retrieve<T>(key)
        if (retrieved == null) {
            val newVal = fallback()
            store(key, type, newVal)
            return newVal
        }
        return retrieved
    }
    
    suspend fun <T: Entity> update(key: RestEndpoint.Path, new: JsonObject, constructor: () -> T) {
        val retrieved = retrieve<T>(key)
        if (retrieved == null) store(key, StoreType.PASSIVE, constructor())
        else retrieved.map = new
    }
}
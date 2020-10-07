package me.deprilula28.discordproxykt.cache

interface Cache {
    suspend fun store(key: Any, value: Any): Boolean
    suspend fun <T: Any> retrieve(key: Any): T?
    
    suspend fun <T: Any> get(key: Any, fallback: () -> T): T {
        val retrieved = retrieve<T>(key)
        if (retrieved == null) {
            val newVal = fallback()
            store(key, newVal)
            return newVal
        }
        return retrieved
    }
}
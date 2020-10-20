package me.deprilula28.discordproxykt.rest

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.deprilula28.discordproxykt.DiscordProxyKt

class LargeAction<T>(override val bot: DiscordProxyKt): IRestAction<List<T>> {
    data class Chunk<T>(val index: Int, val count: Int, val data: List<T>)
    private val fullDataDeferred: CompletableDeferred<List<T>> = CompletableDeferred()
    private val chunks = mutableListOf<Chunk<T>>()
    
    fun chunkReceivedCallback(chunk: Chunk<T>) {
        chunks += chunk
        if (chunks.size == chunk.count) fullDataDeferred.complete(chunks.flatMap { it.data })
    }
    
    override suspend fun await(): List<T> = fullDataDeferred.await()
    @ExperimentalCoroutinesApi override suspend fun getIfAvailable(): List<T>? = if (fullDataDeferred.isCompleted) fullDataDeferred.getCompleted() else null
}

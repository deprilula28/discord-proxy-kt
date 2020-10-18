package me.deprilula28.discordproxykt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import me.deprilula28.discordproxykt.cache.DiscordRestCache
import me.deprilula28.discordproxykt.cache.MemoryCache
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Call [build] on this type to construct a [DiscordProxyKt] instance.
 *
 * # Required Parameters
 *
 * @param group - AMQP group defined for Spectacles
 * @param subgroup - The unique identifier for this worker, must be different for each instance you run
 * @param broker - URI used to connect to the AMQP broker (RabbitMQ)
 * @param token - The token used in requests to Discord
 *
 * # Optional Parameters
 *
 * @param coroutineScope - The coroutine scope that should be used for suspend actions. By default, [GlobalScope].
 * @param httpClient - The HTTP Client that should be used for Rest Actions.
 * @param cache - The cache solution. By default, [MemoryCache].
 * @param defaultExceptionHandler - The handler for failed rest actions and event handles.
 * @param deleteQueuesAfter - Whether to remove the AMQP queues after this process closes.<br>
 * Notice that this just informs the connection of its preference, instead of actually performing the removal.
 */
class DpkBuilder(
    var group: String,
    var subgroup: String,
    var broker: URI,
    var token: String?,
    var coroutineScope: CoroutineScope = GlobalScope,
    var httpClient: HttpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(30L))
        .build(),
    var cache: DiscordRestCache = MemoryCache(coroutineScope, 5L to TimeUnit.MINUTES),
    var defaultExceptionHandler: (Exception) -> Unit = { it.printStackTrace() },
    var deleteQueuesAfter: Boolean = false,
) {
    fun build(): DiscordProxyKt = DiscordProxyKt(group, subgroup, broker, coroutineScope, httpClient, token, cache, defaultExceptionHandler, deleteQueuesAfter)
}

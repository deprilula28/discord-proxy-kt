package me.deprilula28.discordproxykt

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import me.deprilula28.discordproxykt.cache.DiscordRestCache
import me.deprilula28.discordproxykt.cache.MemoryCache
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Call [build] on this type to construct a [DiscordProxyKt] instance.
 *
 * # Required Parameters
 *
 * @param group - AMQP group defined for Spectacles
 * @param subgroup - The unique identifier for this worker, must be different for each instance you run
 * @param token - The token used in requests to Discord
 *
 * # Provide One Of
 *
 * @param brokerUri - URI used to connect to the AMQP broker
 * @param broker - Existing connection to use for the AMQP broker
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
    var token: String,
    var brokerUri: URI? = null,
    var broker: Connection? = null,
    var coroutineScope: CoroutineScope = GlobalScope,
    var httpClient: HttpClient = HttpClient(Apache) {
        engine {
            connectTimeout = 30_000
            socketTimeout = 20_000
            connectionRequestTimeout = 20_000
        }
    },
    var cache: DiscordRestCache = MemoryCache(coroutineScope, 5L to TimeUnit.MINUTES),
    var defaultExceptionHandler: (Exception) -> Unit = { it.printStackTrace() },
    var deleteQueuesAfter: Boolean = false,
) {
    fun build() = SpectaclesAmqpDpk(group, subgroup, broker ?: run {
        val factory = ConnectionFactory()
        factory.setUri(brokerUri ?: throw ExceptionInInitializerError("Connection or broker must be provided"))
        factory.newConnection()
    }, coroutineScope, httpClient, token, cache, defaultExceptionHandler, deleteQueuesAfter)
}

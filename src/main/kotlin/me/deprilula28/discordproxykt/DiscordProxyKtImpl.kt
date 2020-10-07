package me.deprilula28.discordproxykt

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import me.deprilula28.discordproxykt.events.EventConsumer
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.getOrSet

class DiscordProxyKtImpl internal constructor(
    private val group: String,
    private val subgroup: String,
    broker: URI,
    val scope: CoroutineScope,
    val client: HttpClient,
    token: String?,
): DiscordProxyKt {
    internal val authorization = "Bot $token"
    private val conn: Connection
    private val channels = ThreadLocal<Channel>()
    val pool: ExecutorService = Executors.newWorkStealingPool()
    
    val channel: Channel
        get() = channels.getOrSet {
            val channel = conn.createChannel()
            channel.exchangeDeclare(group, "direct", true, false, mapOf())
            channel
        }
    
    init {
        val factory = ConnectionFactory()
        factory.setUri(broker)
        conn = factory.newConnection()
    }
    
    fun subscribe(vararg events: String) {
        val channel = this.channel
        events.forEach { event ->
            val queue = channel.queueDeclare("$group:$subgroup:$event", true, false, false, mapOf()).queue
            channel.queueBind(queue, group, event)
            channel.basicConsume(queue, false, EventConsumer(this, event, channel))
        }
    }
    
    companion object {
        class Builder(var group: String, var subgroup: String, var broker: URI, var token: String?) {
            var coroutineScope: CoroutineScope = GlobalScope
            var httpClient: HttpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(30L))
                .build()
            
            fun build(): DiscordProxyKt = DiscordProxyKtImpl(group, subgroup, broker, coroutineScope, httpClient,
                                                         token)
        }
    }
}

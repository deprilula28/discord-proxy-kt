package me.deprilula28.discordproxykt

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import me.deprilula28.discordproxykt.cache.Cache
import me.deprilula28.discordproxykt.cache.MemoryCache
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.PartialRole
import me.deprilula28.discordproxykt.entities.discord.PartialTextChannel
import me.deprilula28.discordproxykt.entities.discord.Role
import me.deprilula28.discordproxykt.entities.discord.TextChannel
import me.deprilula28.discordproxykt.events.EventConsumer
import me.deprilula28.discordproxykt.rest.ReadyRestAction
import me.deprilula28.discordproxykt.rest.RestAction
import me.deprilula28.discordproxykt.rest.RestEndpoint
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.getOrSet

class DiscordProxyKt internal constructor(
    private val group: String,
    private val subgroup: String,
    broker: URI,
    val scope: CoroutineScope,
    val client: HttpClient,
    token: String?,
    val cache: Cache,
) {
    internal val authorization = "Bot $token"
    private val conn: Connection
    private val amqpChannels = ThreadLocal<Channel>()
    val pool: ExecutorService = Executors.newWorkStealingPool()
    
    val amqpChannel: Channel
        get() = amqpChannels.getOrSet {
            val channel = conn.createChannel()
            channel.exchangeDeclare(group, "direct", true, false, mapOf())
            channel
        }
    
    class TextChannels(private val bot: DiscordProxyKt) {
        operator fun get(id: Snowflake): PartialTextChannel.Upgradeable {
            val ret = bot.cache.retrieve<TextChannel>(id)
            return if (ret == null) object: PartialTextChannel.Upgradeable,
                RestAction<TextChannel>(bot, ::TextChannel, RestEndpoint.GET_CHANNEL, id.id) {
                override val snowflake: Snowflake = id
            }
            else object: PartialTextChannel.Upgradeable, ReadyRestAction<TextChannel>(ret, bot) {
                override val snowflake = id
            }
        }
    }
    val channels = TextChannels(this)
    
    init {
        val factory = ConnectionFactory()
        factory.setUri(broker)
        conn = factory.newConnection()
    }
    
    fun subscribe(vararg events: String) {
        val channel = amqpChannel
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
            var cache: Cache = MemoryCache(coroutineScope, 5L to TimeUnit.MINUTES)
            
            fun build(): DiscordProxyKt = DiscordProxyKt(group, subgroup, broker, coroutineScope, httpClient, token,
                                                         cache)
        }
    }
}

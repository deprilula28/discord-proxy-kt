package me.deprilula28.discordproxykt

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.cache.DiscordRestCache
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.channel.PartialPrivateChannel
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.events.Event
import me.deprilula28.discordproxykt.events.EventConsumer
import me.deprilula28.discordproxykt.events.Events
import me.deprilula28.discordproxykt.rest.RestAction
import me.deprilula28.discordproxykt.rest.RestEndpoint
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.getOrSet

open class DiscordProxyKt internal constructor(
    private val group: String,
    private val subgroup: String,
    broker: URI,
    val scope: CoroutineScope,
    val client: HttpClient,
    token: String?,
    val cache: DiscordRestCache,
    val defaultExceptionHandler: (Exception) -> Unit,
    val deleteQueuesAfter: Boolean,
) {
    internal val authorization = "Bot $token"
    private val conn: Connection
    private val amqpChannels = ThreadLocal<Channel>()
    val pool: ExecutorService = Executors.newWorkStealingPool()
    val handlerSequence = mutableMapOf<Events.Event<*>, AtomicInteger>()
    
    val amqpChannel: Channel
        get() = amqpChannels.getOrSet {
            val channel = conn.createChannel()
            channel.exchangeDeclare(group, "direct", true, false, mapOf())
            channel
        }
    
    val selfUser: RestAction<User>
        get() = request(RestEndpoint.GET_CURRENT_USER.path(), { User(this as JsonObject, this@DiscordProxyKt) })
    
    fun fetchGuild(snowflake: Snowflake) = PartialGuild.new(snowflake, this)
    fun fetchUser(snowflake: Snowflake) = PartialUser.new(snowflake, this)
    fun fetchPrivateChannel(snowflake: Snowflake) = PartialPrivateChannel.new(snowflake, this)
    
    init {
        val factory = ConnectionFactory()
        factory.setUri(broker)
        conn = factory.newConnection()
    }
    
    fun <T: Event> on(event: Events.Event<T>, handler: suspend (T) -> Unit) {
        synchronized(handlerSequence) {
            val atomic = handlerSequence[event] ?: AtomicInteger(-1).apply { handlerSequence[event] = this }
            val seq = atomic.addAndGet(1)
            val channel = amqpChannel
            val queue = channel.queueDeclare(
                "$group:$subgroup-$seq:${event.eventName}",
                true, deleteQueuesAfter,
                deleteQueuesAfter, mapOf()
            ).queue
            channel.queueBind(queue, group, event.eventName)
            channel.basicConsume(queue, false, EventConsumer(this, event, handler, channel))
        }
    }
    
    fun <T: Any> request(path: RestEndpoint.Path, constructor: JsonElement.(DiscordProxyKt) -> T, postData: (() -> String)? = null)
            = RestAction(this, path, constructor, postData)
    // TODO better way
    suspend fun <T: Any> coroutineRequest(path: RestEndpoint.Path, constructor: JsonElement.(DiscordProxyKt) -> T, postData: (() -> String)? = null)
            = RestAction(this, path, constructor, postData).await()
}

package me.deprilula28.discordproxykt

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.cache.DiscordRestCache
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.entities.discord.User
import me.deprilula28.discordproxykt.entities.discord.channel.PartialPrivateChannel
import me.deprilula28.discordproxykt.entities.discord.guild.Member
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.events.Event
import me.deprilula28.discordproxykt.events.EventConsumer
import me.deprilula28.discordproxykt.events.Events
import me.deprilula28.discordproxykt.rest.InvalidRequestException
import me.deprilula28.discordproxykt.rest.LargeAction
import me.deprilula28.discordproxykt.rest.RestAction
import me.deprilula28.discordproxykt.rest.RestEndpoint
import java.net.URI
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.getOrSet

open class SpectaclesAmqpDpk internal constructor(
    private val group: String,
    private val subgroup: String,
    broker: URI,
    scope: CoroutineScope,
    client: HttpClient,
    token: String?,
    cache: DiscordRestCache,
    defaultExceptionHandler: (Exception) -> Unit,
    val deleteQueuesAfter: Boolean,
): DiscordProxyKt(scope, client, token, cache, defaultExceptionHandler) {
    private val conn: Connection
    private val amqpChannels = ThreadLocal<Channel>()
    val pool: ExecutorService = Executors.newWorkStealingPool()
    val handlerSequence = mutableMapOf<Events.Event<*>, AtomicInteger>()
    val largeRequests = ConcurrentHashMap<String, LargeAction<*>>()
    
    val amqpChannel: Channel
        get() = amqpChannels.getOrSet {
            val channel = conn.createChannel()
            channel.exchangeDeclare(group, "direct", true, false, mapOf())
            channel
        }
    
    private fun <T> largeRequest(nonce: String, gatewayGuildId: String, input: JsonElement): LargeAction<T> {
        val action = LargeAction<T>(this)
        val str = Json.encodeToString(mapOf(
            "guild_id" to JsonPrimitive(gatewayGuildId),
            "packet" to Json.encodeToJsonElement(mapOf(
                "op" to JsonPrimitive(8),
                "d" to input
            ))
        ))
        amqpChannel.basicPublish(group, "SEND", null, str.toByteArray())
        largeRequests[nonce] = action
        
        return action
    }
    
    init {
        val factory = ConnectionFactory()
        factory.setUri(broker)
        conn = factory.newConnection()
        
        on(Events.GUILD_MEMBERS_CHUNK) {
            @Suppress("UNCHECKED_CAST")
            val action: LargeAction<Member> = largeRequests[it.nonce] as? LargeAction<Member> ?: return@on
            action.chunkReceivedCallback(LargeAction.Chunk(it.chunkIndex, it.chunkCount, it.members))
            largeRequests.remove(it.nonce)
        }
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
    
    fun requestGuildMembers(
        guilds: List<Snowflake>,
        presences: Boolean = false, // TODO Presences
        query: String? = null,
        limit: Int? = null,
        users: List<Snowflake>? = null,
    ): LargeAction<Member> {
        if (query != null && limit == null) throw InvalidRequestException("Cannot create request with a query and no limit!")
        if (guilds.isEmpty()) throw InvalidRequestException("At least one guild must be supplied!")
        
        var nonce: String
        do {
            nonce = UUID.randomUUID().toString().replace("-", "")
        } while (largeRequests.containsKey(nonce))
        val map = mutableMapOf(
            "nonce" to JsonPrimitive(nonce),
            "presences" to JsonPrimitive(presences),
            "guild_id" to Json.encodeToJsonElement(guilds.map(Snowflake::id)),
        )
        limit?.apply { map["limit"] = JsonPrimitive(this) }
        query?.apply { map["query"] = JsonPrimitive(this) }
        users?.apply { map["userids"] = Json.encodeToJsonElement(map(Snowflake::id)) }
        
        return largeRequest(nonce, guilds[0].id, Json.encodeToJsonElement(map))
    }
}
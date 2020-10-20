package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.guild.BoostTier
import me.deprilula28.discordproxykt.entities.discord.guild.Member
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.rest.*

/**
 * This type is used for operations when an ID of a [VoiceChannel] is known.<br>
 * If the data is also known it will implement [VoiceChannel], and [upgrade] is a no-op.<br>
 * If it isn't known, [upgrade] will be a request to get the data from Discord.
 */
interface PartialVoiceChannel: PartialGuildChannel, PartialEntity {
    companion object {
        fun new(guild: PartialGuild, id: Snowflake): PartialVoiceChannel
            = object: PartialVoiceChannel {
                override val snowflake: Snowflake = id
                override val bot: DiscordProxyKt = guild.bot
        
                override fun upgrade(): IRestAction<VoiceChannel>
                    = bot.request(RestEndpoint.GET_CHANNEL.path(snowflake.id), { VoiceChannel(this as JsonObject, bot) })
                override fun toString(): String = "Channel(partial, $type, $guild, $snowflake.id)"
            }
    }
    
    override val type: ChannelType
        get() = ChannelType.VOICE
    
    override fun upgrade(): IRestAction<VoiceChannel>
}

open class VoiceChannel(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), GuildChannel, PartialVoiceChannel, EntityManager<VoiceChannel> {
    /**
     * the bitrate (in bits) of the voice channel
     */
    var bitrate: Int by parsing(JsonElement::asInt, Json::encodeToJsonElement)
    /**
     * the user limit of the voice channel
     */
    var userLimit: Int by parsing(JsonElement::asInt, {
        if (it !in 0 .. 99) throw InvalidRequestException("User limit must be 0 for no limit or 1-99 for a user limit")
        Json.encodeToJsonElement(it)
    }, "user_limit")
    
    override val guild: PartialGuild by parsing({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    
    override var name: String by parsing(JsonElement::asString, {
        if (it.length !in 2 .. 100) throw InvalidRequestException("Channel name must be between 2 and 100 characters in length")
        Json.encodeToJsonElement(it)
    })
    override var position: Int by parsing(JsonElement::asInt, Json::encodeToJsonElement)
    override var category: PartialCategory? by parsingOpt(
        { PartialCategory.new(guild, asSnowflake()) },
        { it?.run { JsonPrimitive(snowflake.id) } ?: JsonNull },
        "parent_id",
    )
    override var permissions: List<PermissionOverwrite> by parsing({
        (this as JsonArray).map { it.asPermissionOverwrite(this@VoiceChannel, guild, bot) }
    }, { Json.encodeToJsonElement(it.map(PermissionOverwrite::toObject)) }, "permission_overwrites")
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * Requests that this guild gets edited based on the altered fields.
     */
    override fun edit(): IRestAction<VoiceChannel> {
        if (changes.isEmpty()) throw InvalidRequestException(
            "No changes have been made to this channel, yet `edit()` was called.")
        return IRestAction.coroutine(guild.bot) {
            assertPermissions(this, Permissions.MANAGE_CHANNELS)
            if (changes.containsKey("bitrate") && (changes["bitrate"] as JsonPrimitive).asInt() !in 8000 ..
                when (guild.upgrade().await().boostTier) {
                    BoostTier.NONE -> 96000
                    BoostTier.TIER_1 -> 128000
                    BoostTier.TIER_2 -> 256000
                    BoostTier.TIER_3 -> 384000
                }) throw InvalidRequestException("Invalid bitrate for guild boost tier")
            bot.request(RestEndpoint.MODIFY_CHANNEL.path(snowflake.id),
                        { this@VoiceChannel.apply { map = this@request as JsonObject } }) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }.await()
        }
    }
    
    fun createCopy(guild: PartialGuild) = VoiceChannelBuilder(guild, bot).apply {
        name = this@VoiceChannel.name
        position = this@VoiceChannel.position
        permissions = this@VoiceChannel.permissions
        category = this@VoiceChannel.category
        userLimit = this@VoiceChannel.userLimit
        bitrate = this@VoiceChannel.bitrate
    }
    fun createCopy() = createCopy(guild)
    
    override fun upgrade(): IRestAction<VoiceChannel> = IRestAction.ProvidedRestAction(bot, this)
    
    override fun toString(): String = "Channel($type, $guild, $name, ${snowflake.id})"
    
    fun fetchCanSpeak() = IRestAction.coroutine(bot) {
        fetchPermissions(guild.fetchSelfMember.await()).await().contains(Permissions.SPEAK)
    }
    fun fetchCanSpeak(member: Member) = IRestAction.coroutine(bot) {
        fetchPermissions(member).await().contains(Permissions.SPEAK)
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("fetchCanSpeak()"))
    fun canSpeak() = fetchCanSpeak().complete()
    @Deprecated("JDA Compatibility Field", ReplaceWith("fetchCanSpeak(member)"))
    fun canSpeak(member: Member) = fetchCanSpeak(member).complete()
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("category?.upgrade()?.request()?.get()"))
    val parent: Category?
        get() = category?.upgrade()?.complete()
}

class VoiceChannelBuilder(private val internalGuild: PartialGuild, bot: DiscordProxyKt):
    VoiceChannel(JsonObject(MapNotReady()), bot), EntityBuilder<VoiceChannel>
{
    /**
     * Creates a voice channel based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a [VoiceChannel].
     */
    override fun create(): IRestAction<VoiceChannel> {
        if (!changes.containsKey("name")) throw InvalidRequestException("Channels require at least a name.")
        changes["type"] = JsonPrimitive(2)
        return IRestAction.coroutine(internalGuild.bot) {
            assertPermissions(internalGuild, Permissions.MANAGE_GUILD)
            bot.request(
                RestEndpoint.CREATE_GUILD_CHANNEL.path(internalGuild.snowflake.id),
                { this@VoiceChannelBuilder.apply { map = this@request as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }.await()
        }
    }
    override fun toString(): String = "Channel(builder)"
}

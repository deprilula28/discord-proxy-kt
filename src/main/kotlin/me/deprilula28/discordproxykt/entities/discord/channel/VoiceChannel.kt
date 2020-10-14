package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.ChannelType
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PermissionOverwrite
import me.deprilula28.discordproxykt.entities.discord.Permissions
import me.deprilula28.discordproxykt.rest.*

/**
 * a voice channel within a server
 * <br>
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
interface PartialVoiceChannel: PartialEntity {
    companion object {
        fun new(guild: PartialGuild, id: Snowflake): PartialVoiceChannel
                = object: PartialVoiceChannel {
            override val snowflake: Snowflake = id
            override val bot: DiscordProxyKt = guild.bot
    
            override fun upgrade(): IRestAction<VoiceChannel> =
                IRestAction.FuturesRestAction(guild.bot) {
                    guild.fetchChannels.request().thenApply {
                        it.find { ch -> ch.snowflake == id } as VoiceChannel
                    }
                }
        }
    }
    
    fun upgrade(): IRestAction<VoiceChannel>
}

open class VoiceChannel(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), GuildChannel, PartialVoiceChannel, EntityManager<VoiceChannel> {
    /**
     * the bitrate (in bits) of the voice channel
     */
    val bitrate: Int by map.delegateJson(JsonElement::asInt)
    /**
     * the user limit of the voice channel
     */
    val userLimit: Int by map.delegateJson(JsonElement::asInt, "user_limit")
    
    override val guild: PartialGuild by map.delegateJson({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val name: String by map.delegateJson(JsonElement::asString)
    override val category: PartialCategory? by map.delegateJsonNullable({ PartialCategory.new(guild, asSnowflake()) }, "parent_id")
    
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map { asPermissionOverwrite(this@VoiceChannel, guild, bot) }
    }, "permission_overwrites")
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * Requests that this guild gets edited based on the altered fields.
     */
    override fun edit(): IRestAction<VoiceChannel> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this channel, yet `edit()` was called.")
        return assertPermissions(this, Permissions.MANAGE_CHANNELS) {
            bot.request(RestEndpoint.MODIFY_CHANNEL.path(snowflake.id), { this@VoiceChannel.apply { map = this@request as JsonObject } }) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
    
    fun createCopy(guild: PartialGuild) = VoiceChannelBuilder(guild, bot).apply {
        // TODO fill this with copying fields
    }
    fun createCopy() = createCopy(guild)
    
    override fun upgrade(): IRestAction<VoiceChannel> = IRestAction.ProvidedRestAction(bot, this)
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("category?.upgrade()?.request()?.get()"))
    val parent: Category?
        get() = category?.upgrade()?.request()?.get()
    
    override val type: ChannelType
        get() = ChannelType.VOICE
}

class VoiceChannelBuilder(private val internalGuild: PartialGuild, bot: DiscordProxyKt):
    VoiceChannel(JsonObject(MapNotReady()), bot), EntityBuilder<VoiceChannel>
{
    /**
     * Creates a voice channel based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a VoiceChannel.
     */
    override fun create(): IRestAction<VoiceChannel> {
        if (!changes.containsKey("name")) throw InvalidRequestException("Channels require at least a name.")
        changes["type"] = JsonPrimitive(2)
        return assertPermissions(this, Permissions.MANAGE_GUILD) {
            bot.request(
                RestEndpoint.CREATE_GUILD_CHANNEL.path(internalGuild.snowflake.id),
                { this@VoiceChannelBuilder.apply { map = this@request as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
}

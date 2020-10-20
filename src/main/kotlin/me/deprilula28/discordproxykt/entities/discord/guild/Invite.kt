package me.deprilula28.discordproxykt.entities.discord.guild

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.Parse
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.UnavailableField
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.channel.GuildChannel
import me.deprilula28.discordproxykt.entities.discord.channel.PartialGuildChannel
import me.deprilula28.discordproxykt.events.guild.invite.*
import me.deprilula28.discordproxykt.rest.*

/**
 * https://discord.com/developers/docs/resources/invite#invite-object
 */
open class Invite(
    override var map: JsonObject,
    override val bot: DiscordProxyKt,
    private val internalGuild: PartialGuild? = null
): Parse {
    val code: String by parsing(JsonElement::asString)
    /**
     * The guild returned from the invite is a preview guild, with less information.
     */
    // TODO Preview guild object
    val guild: Guild? by parsingOpt({ Guild(this as JsonObject, bot) })
    val inviter: User? by parsingOpt({ User(this as JsonObject, bot) })
    val targetUser: User? by parsingOpt({ User(this as JsonObject, bot) }, "target_user")
    val approxPresenceCount: Int? by parsingOpt(JsonElement::asInt, "approximate_presence_count")
    val approxMemberCount: Int? by parsingOpt(JsonElement::asInt, "approximate_member_count")
    
    /**
     * This is present under fetched invites
     */
    val cachedChannel: GuildChannel? by parsingOpt({
        (guild ?: internalGuild)?.run {
            this@parsingOpt.asGuildChannel(bot, this)
        }
    }, "channel")
    /**
     * This is present under invite events [GuildInviteCreateEvent] and [GuildInviteDeleteEvent]
     */
    val partialChannel: PartialGuildChannel? by parsingOpt({
        (guild ?: internalGuild)?.run {
            PartialGuildChannel.new(this, asSnowflake())
        }
    }, "channel_id")
    
    /**
     * The only case where this is not present is when [guild] is `null`
     */
    val channel: PartialGuildChannel?
        get() = cachedChannel ?: partialChannel
    
    fun delete(): IRestAction<Unit> {
        return if (guild != null) IRestAction.coroutine(bot) {
            assertPermissions(guild!!, Permissions.MANAGE_GUILD)
            bot.coroutineRequest(RestEndpoint.DELETE_INVITE.path(code), { Unit })
        }
        else bot.request(RestEndpoint.DELETE_INVITE.path(code), { Unit })
    }
    
    /**
     * @throws [UnavailableField] If [guild] is `null`
     */
    fun expand(): IRestAction<ExtendedInvite>
        = IRestAction.coroutine(bot) {
        (channel ?: throw UnavailableField()).fetchInvites.await().find { it.code == this.code }
                ?: throw UnavailableField()
    }
}

class InviteBuilder(private val internalChannel: GuildChannel, bot: DiscordProxyKt):
    Invite(JsonObject(MapNotReady()), bot), EntityBuilder<Invite>
{
    val changes = mutableMapOf<String, JsonElement>()
    
    /**
     * Creates an invite based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a Invite.
     */
    override fun create(): IRestAction<Invite> {
        return IRestAction.coroutine(bot) {
            assertPermissions(internalChannel, Permissions.CREATE_INSTANT_INVITE)
            bot.coroutineRequest(
                RestEndpoint.CREATE_CHANNEL_INVITE.path(internalChannel.snowflake.id),
                { this@InviteBuilder.apply { map = this@coroutineRequest as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
}

class ExtendedInvite(map: JsonObject, bot: DiscordProxyKt, internalGuild: PartialGuild? = null):
    Invite(map, bot, internalGuild)
{
    val uses: Int by parsing(JsonElement::asInt, "uses")
    val maxUses: Int by parsing(JsonElement::asInt, "max_uses")
    val maxAge: Int by parsing(JsonElement::asInt, "max_age")
    val temporary: Boolean by parsing(JsonElement::asBoolean)
    val createdAt: Timestamp by parsing(JsonElement::asTimestamp, "created_at")
}
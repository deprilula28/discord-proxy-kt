package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.rest.*

// TODO Fill this
interface PartialMember {
    val guild: PartialGuild
    val user: PartialUser
    val bot: DiscordProxyKt
    
    fun add(role: PartialRole): IRestAction<Unit>
            = RestAction(bot, { Unit }, RestEndpoint.ADD_GUILD_MEMBER_ROLE, guild.snowflake.id, user.snowflake.id, role.snowflake.id)
    
    fun remove(role: PartialRole): IRestAction<Unit>
            = RestAction(bot, { Unit }, RestEndpoint.REMOVE_GUILD_MEMBER_ROLE, guild.snowflake.id, user.snowflake.id, role.snowflake.id)
    
    interface Upgradeable: PartialMember, IRestAction<Member>
}

/**
 * a user within a guild
 * <br>
 * https://discord.com/developers/docs/resources/guild#guild-member-object
 */
class Member(override val guild: PartialGuild, private val map: JsonObject, override val bot: DiscordProxyKt, readyUser: User? = null): PartialMember, EntityManager<Member> {
    /**
     * the user this guild member represents
     */
    override val user: User by lazy { readyUser ?: User(map["user"] as JsonObject, bot) }
    /**
     * this users guild nickname
     */
    var nick: String? by map.delegateJsonMutableNullable(JsonElement::asString, Json::encodeToJsonElement)
    /**
     * array of role object ids
     */
    val roles: List<Snowflake> by map.delegateJsonMutable(
        { (this as JsonArray).map { it.asSnowflake() } },
        { Json.encodeToJsonElement(it.map { sn -> sn.id }) }
    )
    /**
     * when the user joined the guild
     */
    val joinedAt: Timestamp by map.delegateJson(JsonElement::asTimestamp, "joined_at")
    /**
     * when the user started boosting the guild
     */
    val premiumSince: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "premium_since")
    /**
     * whether the user is deafened in voice channels
     */
    var deaf: Boolean by map.delegateJsonMutable(JsonElement::asBoolean, Json::encodeToJsonElement)
    /**
     * whether the user is muted in voice channels
     */
    var mute: Boolean by map.delegateJsonMutable(JsonElement::asBoolean, Json::encodeToJsonElement)
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * To finish the procedure, the rest action needs to be called.
     */
    fun move(channel: PartialVoiceChannel?): Member {
        changes["channel_id"] = Json.encodeToJsonElement(channel?.snowflake?.id)
        return this
    }
    
    /**
     * Requests that this guild gets edited based on the altered fields.<br>
     * This object will not be updated to reflect the changes, rather a new Webhook object is returned from the RestAction.
     */
    override fun edit(): IRestAction<Member> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this webhook, yet `edit()` was called.")
        return RestAction(bot, { Member(guild, this as JsonObject, bot) }, RestEndpoint.MODIFY_GUILD_MEMBER, guild.snowflake.id, user.snowflake.id) {
            val res = Json.encodeToString(changes)
            changes.clear()
            res
        }
    }
}
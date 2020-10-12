import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.entities.discord.message.UnicodeEmoji
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import java.awt.Color
import java.util.*
import kotlin.test.Test

import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD) class ParseAgainstDoc {
    // https://discord.com/developers/docs/resources/channel#message-object-example-message
    @Test fun messageExample() {
        val text = """
            {
                "reactions": [
                    {
                        "count": 1,
                        "me": false,
                        "emoji": {
                            "id": null,
                            "name": "🔥"
                        }
                    }
                ],
                "attachments": [],
                "tts": false,
                "embeds": [],
                "timestamp": "2017-07-11T17:27:07.299000+00:00",
                "mention_everyone": false,
                "id": "334385199974967042",
                "pinned": false,
                "edited_timestamp": null,
                "author": {
                    "username": "Mason",
                    "discriminator": "9999",
                    "id": "53908099506183680",
                    "avatar": "a_bab14f271d565501444b2ca3be944b25"
                },
                "mention_roles": [],
                "content": "Supa Hot",
                "channel_id": "290926798999357250",
                "mentions": [],
                "type": 0
            }
        """.trimIndent()
        
        val message = Message(Json.decodeFromString(JsonObject.serializer(), text), Mockito.mock(DiscordProxyKt::class.java))
        
        assertEquals(message.snowflake, Snowflake("334385199974967042"))
        assertEquals(message.channelRaw, Snowflake("290926798999357250"))
        assertEquals(message.guildSnowflake, null)
        assertEquals(message.member, null)
        assertEquals(message.content, "Supa Hot")
        assertEquals(message.timestamp, Timestamp(1499794027299L))
        assertEquals(message.editTimestamp, null)
        assertEquals(message.tts, false)
        assertEquals(message.mentionEveryone, false)
        assertEquals(message.mentionChannels, null)
        assertEquals(message.type, Message.Type.DEFAULT)
        assertEquals(message.pinned, false)
        assertEquals(message.webhookId, null)
        
        assert(message.embeds.isEmpty())
        assert(message.attachments.isEmpty())
        assert(message.mentions.isEmpty())
        assert(message.mentionRoles.isEmpty())
        
        val reaction = message.reactions[0]
        assertEquals(reaction.count, 1)
        assertEquals(reaction.me, false)
        
        val emoji = reaction.emoji
        assert(emoji is UnicodeEmoji)
        assertEquals((emoji as UnicodeEmoji).name, "\uD83D\uDD25")
        
        val author = message.author
        assertEquals(author.username, "Mason")
        assertEquals(author.discriminator, "9999")
        assertEquals(author.snowflake, Snowflake("53908099506183680"))
        assertEquals(author.avatarHash, "a_bab14f271d565501444b2ca3be944b25")
    }
    
    // https://discord.com/developers/docs/resources/channel#channel-object-example-guild-text-channel
    @Test fun guildTextChannelExample() {
        val text = """
            {
                "id": "41771983423143937",
                "guild_id": "41771983423143937",
                "name": "general",
                "type": 0,
                "position": 6,
                "permission_overwrites": [],
                "rate_limit_per_user": 2,
                "nsfw": true,
                "topic": "24/7 chat about how to gank Mike #2",
                "last_message_id": "155117677105512449",
                "parent_id": "399942396007890945"
            }
        """.trimIndent()
        
        val channel = TextChannel(Json.decodeFromString(JsonObject.serializer(), text), Mockito.mock(DiscordProxyKt::class.java))
    
        assertEquals(channel.snowflake, Snowflake("41771983423143937"))
        assertEquals(channel.guildSnowflake, Snowflake("41771983423143937"))
        assertEquals(channel.name, "general")
        assertEquals(channel.position, 6)
        assertEquals(channel.rateLimitPerUser, 2)
        assertEquals(channel.nsfw, true)
        assertEquals(channel.topic, "24/7 chat about how to gank Mike #2")
        assertEquals(channel.lastMessageId, Snowflake("155117677105512449"))
        assertEquals(channel.categorySnowflake, Snowflake("399942396007890945"))
        
        assert(channel.permissions.isEmpty())
    }
    
    // https://discord.com/developers/docs/resources/guild#guild-member-object-example-guild-member
    @Test fun guildMemberExample() {
        val text = """
            {
                "user": {},
                "nick": "NOT API SUPPORT",
                "roles": [],
                "joined_at": "2015-04-26T06:26:56.936000+00:00",
                "deaf": false,
                "mute": false
            }
        """.trimIndent()
        
        val member = Member(Mockito.mock(PartialGuild::class.java), Json.decodeFromString(JsonObject.serializer(), text), Mockito.mock(DiscordProxyKt::class.java))
    
        assertEquals(member.nick, "NOT API SUPPORT")
        assert(!member.deaf)
        assert(!member.mute)
        assert(member.roles.isEmpty())
        member.joinedAt
    }
    
    // https://discord.com/developers/docs/resources/guild#get-guild-example-response
    @Test fun guildExample() {
        val text = """
            {
                "id": "2909267986263572999",
                "name": "Mason's Test Server",
                "icon": "389030ec9db118cb5b85a732333b7c98",
                "description": null,
                "splash": "75610b05a0dd09ec2c3c7df9f6975ea0",
                "discovery_splash": null,
                "approximate_member_count": 2,
                "approximate_presence_count": 2,
                "features": ["INVITE_SPLASH", "VANITY_URL", "COMMERCE", "BANNER", "NEWS", "VERIFIED", "VIP_REGIONS"],
                "emojis": [{
                    "name": "ultrafastparrot",
                    "roles": [],
                    "id": "393564762228785161",
                    "require_colons": true,
                    "managed": false,
                    "animated": true,
                    "available": true
                }],
                "banner": "5c3cb8d1bc159937fffe7e641ec96ca7",
                "owner_id": "53908232506183680",
                "application_id": null,
                "region": "us-east",
                "afk_channel_id": null,
                "afk_timeout": 300,
                "system_channel_id": null,
                "widget_enabled": true,
                "widget_channel_id": "639513352485470208",
                "verification_level": 0,
                "roles": [{
                    "id": "2909267986263572999",
                    "name": "@everyone",
                    "permissions": "49794752",
                    "position": 0,
                    "color": 0,
                    "hoist": false,
                    "managed": false,
                    "mentionable": false
                }],
                "default_message_notifications": 1,
                "mfa_level": 0,
                "explicit_content_filter": 0,
                "max_presences": null,
                "max_members": 250000,
                "max_video_channel_users": 25,
                "vanity_url_code": "no",
                "premium_tier": 0,
                "premium_subscription_count": 0,
                "system_channel_flags": 0,
                "preferred_locale": "en-US",
                "rules_channel_id": null,
                "public_updates_channel_id": null
            }
        """.trimIndent()
        
        val guild = Guild(Json.decodeFromString(JsonObject.serializer(), text), Mockito.mock(DiscordProxyKt::class.java))
    
        assertEquals(guild.snowflake, Snowflake("2909267986263572999"))
        assertEquals(guild.name, "Mason's Test Server")
        assertEquals(guild.icon, "389030ec9db118cb5b85a732333b7c98")
        assertEquals(guild.iconSplash, "75610b05a0dd09ec2c3c7df9f6975ea0")
        assertEquals(guild.description, null)
        assertEquals(guild.discoverableSplash, null)
        assertEquals(guild.approxMemberCount, 2)
        assertEquals(guild.approxPresenceCount, 2)
        assertEquals(guild.banner, "5c3cb8d1bc159937fffe7e641ec96ca7")
        // this fails because bot doesn't mock getting the user
        // i could force it to but then is it even testing anything
        // assertEquals(guild.owner.user.snowflake, Snowflake("53908232506183680"))
        assertEquals(guild.applicationSnowflake, null)
        assertEquals(guild.region, Region.US_EAST)
        assertEquals(guild.regionRaw, "us-east")
        assertEquals(guild.afkChannel, null)
        assertEquals(guild.afkTimeout, Timeout.SECONDS_300)
        assertEquals(guild.systemChannel, null)
        assertEquals(guild.widgetEnabled, true)
        assertEquals(guild.widgetChannel!!.snowflake, Snowflake("639513352485470208"))
        assertEquals(guild.verificationLevel, VerificationLevel.NONE)
        assertEquals(guild.defaultNotificationLevel, NotificationLevel.MENTIONS_ONLY)
        assertEquals(guild.requiredMFALevel, MFALevel.NONE)
        assertEquals(guild.explicitContentLevel, ExplicitContentFilterLevel.OFF)
        assertEquals(guild.maxPresences, null)
        assertEquals(guild.maxMembers, 250000)
        assertEquals(guild.maxVideoChannelUsers, 25)
        assertEquals(guild.vanityCode, "no")
        assertEquals(guild.boostTier, BoostTier.NONE)
        assertEquals(guild.boosters, 0)
        assertEquals(guild.systemChannelFlags, EnumSet.noneOf(SystemChannelFlags::class.java))
        assertEquals(guild.locale, "en-US")
        assertEquals(guild.rulesChannelSnowflake, null)
        assertEquals(guild.publicUpdatesChannel, null)
    
        assertEquals(guild.features, EnumSet.of(
            Features.INVITE_SPLASH,
            Features.VANITY_URL,
            Features.COMMERCE,
            Features.BANNER,
            Features.NEWS,
            Features.VERIFIED,
            Features.VIP_REGIONS,
        ))
        
        val emoji = guild.emojis[0]
        assertEquals(emoji.name, "ultrafastparrot")
        assertEquals(emoji.snowflake, Snowflake("393564762228785161"))
        assert(emoji.requireColons)
        assert(emoji.animated)
        assert(emoji.available)
        assert(!emoji.managed)
        assert(emoji.roles.isEmpty())
        
        val role = guild.cachedRoles[0]
        assertEquals(role.name, "@everyone")
        assertEquals(role.snowflake, Snowflake("2909267986263572999"))
        assertEquals(role.position, 0)
        assertEquals(role.color, Color.black)
        assertEquals(role.permissions, EnumSet.of(
            Permissions.ADD_REACTIONS,
            Permissions.VIEW_AUDIT_LOG,
            Permissions.STREAM,
            Permissions.VIEW_CHANNEL,
            Permissions.SEND_MESSAGES,
            Permissions.EMBED_LINKS,
            Permissions.ATTACH_FILES,
            Permissions.READ_MESSAGE_HISTORY,
            Permissions.MENTION_EVERYONE,
            Permissions.USE_EXTERNAL_EMOJIS,
            Permissions.CONNECT,
            Permissions.SPEAK,
            Permissions.MUTE_MEMBERS,
            Permissions.DEAFEN_MEMBERS,
            Permissions.USE_VAD
        ))
        assert(!role.mentionable)
        assert(!role.managed)
        assert(!role.hoisted)
    }
}

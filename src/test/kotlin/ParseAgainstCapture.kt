import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.channel.TextChannel
import me.deprilula28.discordproxykt.entities.discord.guild.*
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.events.guild.*
import me.deprilula28.discordproxykt.events.guild.invite.GuildInviteCreateEvent
import me.deprilula28.discordproxykt.events.guild.invite.GuildInviteDeleteEvent
import me.deprilula28.discordproxykt.events.guild.member.GuildMemberUpdateEvent
import me.deprilula28.discordproxykt.events.guild.role.RoleCreateEvent
import me.deprilula28.discordproxykt.events.guild.role.RoleDeleteEvent
import me.deprilula28.discordproxykt.events.guild.role.RoleUpdateEvent
import me.deprilula28.discordproxykt.events.message.MessageBulkDeleteEvent
import me.deprilula28.discordproxykt.events.message.MessageDeleteEvent
import me.deprilula28.discordproxykt.events.message.MessageReceivedEvent
import me.deprilula28.discordproxykt.events.message.MessageUpdateEvent
import me.deprilula28.discordproxykt.events.message.reaction.MessageReactionAddEvent
import me.deprilula28.discordproxykt.events.message.reaction.MessageReactionRemoveAllEvent
import me.deprilula28.discordproxykt.events.message.reaction.MessageReactionRemoveEvent
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import java.awt.Color
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD) class ParseAgainstCapture {
    @Test fun messageCreate() {
        val text = """
            {
                "type": 0,
                "tts": false,
                "timestamp": "2020-10-12T05:53:15.947000+00:00",
                "referenced_message": null,
                "pinned": false,
                "nonce": "765089686849847296",
                "mentions": [],
                "mention_roles": [],
                "mention_everyone": false,
                "member": {
                    "roles": ["460582127428435968", "484502130573443072", "484504124059156480", "484503774396809217", "484503145763045376", "484505030943047680", "484505453389152256", "484505464965562369", "484511827170951169", "484522260590493696", "484505463828905994", "484502801863409675", "484498809682919436", "484522326906503172", "484522471282704384", "484844603267219467", "507747686020022274", "484505464432754706", "484517302037905428", "484502332151824405", "507956432449306644", "484504476800122903", "484523177788047364", "507955848610316318", "514947756842811392", "484505442333098018", "484502015007785011"],
                    "premium_since": null,
                    "nick": "BotImplementation",
                    "mute": false,
                    "joined_at": "2017-08-10T17:39:55.480000+00:00",
                    "is_pending": false,
                    "hoisted_role": "460582127428435968",
                    "deaf": false
                },
                "id": "765089686527148033",
                "flags": 0,
                "embeds": [],
                "edited_timestamp": null,
                "content": "m",
                "channel_id": "484530691107717131",
                "author": {
                    "username": "deprilula28",
                    "public_flags": 131712,
                    "id": "197448151064379393",
                    "discriminator": "3609",
                    "avatar": "89a5ba9b9c465a49ee826b04d20b85f5"
                },
                "attachments": [],
                "guild_id": "345259986303057930"
            }
        """.trimIndent()
        
        val event = MessageReceivedEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                         Mockito.mock(DiscordProxyKt::class.java))
        
        val message = event.message
        assertEquals(message.textChannel!!.snowflake, Snowflake("484530691107717131"))
        assertEquals(message.guild!!.snowflake, Snowflake("345259986303057930"))
        assertEquals(message.content, "m")
        assertEquals(message.webhookId, null)
        assertEquals(message.type, Message.Type.DEFAULT)
        assertEquals(message.flags, EnumSet.noneOf(Message.Flags::class.java))
        assertEquals(message.mentionChannels, null)
        assertEquals(message.reactions, null)
        assert(!message.tts)
        assert(!message.pinned)
        assert(!message.mentionEveryone)
        assert(message.mentions.isEmpty())
        assert(message.mentionRoles.isEmpty())
        assert(message.attachments.isEmpty())
        assert(message.embeds.isEmpty())
        
        val author = event.message.author
        assertEquals(author.snowflake, Snowflake("197448151064379393"))
        assertEquals(author.username, "deprilula28")
        assertEquals(author.discriminator, "3609")
        assertEquals(author.avatarHash, "89a5ba9b9c465a49ee826b04d20b85f5")
        assertEquals(author.publicFlags, EnumSet.of(
            User.Flags.BRILLIANCE,
            User.Flags.VERIFIED_EARLY_BOT_DEVELOPER,
            User.Flags.EARLY_SUPPORTER,
        ))
        assertEquals(author.isBot, false)
        assertEquals(author.system, false)
        assertEquals(author.flags, null)
        assertEquals(author.locale, null)
        assertEquals(author.verified, null)
        assertEquals(author.email, null)
        assertEquals(author.multipleFactor, null)
        assertEquals(author.premiumType, null)
        
        val member = event.message.member!!
        assertEquals(member.user.snowflake, author.snowflake)
        assertEquals(member.nick, "BotImplementation")
        assertEquals(member.roles.map { it.snowflake.id }, listOf(
            "460582127428435968", "484502130573443072", "484504124059156480", "484503774396809217",
            "484503145763045376",
            "484505030943047680", "484505453389152256", "484505464965562369", "484511827170951169",
            "484522260590493696",
            "484505463828905994", "484502801863409675", "484498809682919436", "484522326906503172",
            "484522471282704384",
            "484844603267219467", "507747686020022274", "484505464432754706", "484517302037905428",
            "484502332151824405",
            "507956432449306644", "484504476800122903", "484523177788047364", "507955848610316318",
            "514947756842811392",
            "484505442333098018", "484502015007785011",
        ))
        assertEquals(member.premiumSince, null)
        assert(!member.mute)
        assert(!member.deaf)
    }
    
    @Test fun messageUpdate() {
        val text = """
            {
                "type": 0,
                "tts": false,
                "timestamp": "2020-10-14T11:48:10.690000+00:00",
                "pinned": false,
                "mentions": [],
                "mention_roles": [],
                "mention_everyone": false,
                "member": {
                    "roles": ["460582127428435968", "484502130573443072", "484504124059156480", "484503774396809217", "484503145763045376", "484505030943047680", "484505453389152256", "484505464965562369", "484511827170951169", "484522260590493696", "484505463828905994", "484502801863409675", "484498809682919436", "484522326906503172", "484522471282704384", "484844603267219467", "507747686020022274", "484505464432754706", "484517302037905428", "484502332151824405", "507956432449306644", "484504476800122903", "484523177788047364", "507955848610316318", "514947756842811392", "484505442333098018", "484502015007785011"],
                    "premium_since": null,
                    "nick": "BotImplementation",
                    "mute": false,
                    "joined_at": "2017-08-10T17:39:55.480000+00:00",
                    "is_pending": false,
                    "hoisted_role": "460582127428435968",
                    "deaf": false
                },
                "id": "765903778884747295",
                "flags": 0,
                "embeds": [],
                "edited_timestamp": "2020-10-15T01:34:19.071909+00:00",
                "content": "lol",
                "channel_id": "484530691107717131",
                "author": {
                    "username": "deprilula28",
                    "public_flags": 131712,
                    "id": "197448151064379393",
                    "discriminator": "3609",
                    "avatar": "89a5ba9b9c465a49ee826b04d20b85f5"
                },
                "attachments": [],
                "guild_id": "345259986303057930"
            }
        """.trimIndent()
        
        val event = MessageUpdateEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                            Mockito.mock(DiscordProxyKt::class.java))
    
        assertEquals(event.channel.snowflake, Snowflake("484530691107717131"))
        assertEquals(event.guild.snowflake, Snowflake("345259986303057930"))
        
        val member = event.member!!
        assertEquals(member.roles.map { it.snowflake.id }, listOf(
            "460582127428435968", "484502130573443072", "484504124059156480", "484503774396809217", "484503145763045376",
            "484505030943047680", "484505453389152256", "484505464965562369", "484511827170951169", "484522260590493696",
            "484505463828905994", "484502801863409675", "484498809682919436", "484522326906503172", "484522471282704384",
            "484844603267219467", "507747686020022274", "484505464432754706", "484517302037905428", "484502332151824405",
            "507956432449306644", "484504476800122903", "484523177788047364", "507955848610316318", "514947756842811392",
            "484505442333098018", "484502015007785011"
        ))
        assertEquals(member.nick, "BotImplementation")
        assert(!member.mute)
        assert(!member.deaf)
        
        val author = event.author
        assertEquals(author.username, "deprilula28")
        assertEquals(author.discriminator, "3609")
        assertEquals(author.avatarHash, "89a5ba9b9c465a49ee826b04d20b85f5")
        assertEquals(author.snowflake, Snowflake("197448151064379393"))
        assertEquals(author.publicFlags, EnumSet.of(
            User.Flags.BRILLIANCE,
            User.Flags.VERIFIED_EARLY_BOT_DEVELOPER,
            User.Flags.EARLY_SUPPORTER,
        ))
        
        val message = event.message
        assertEquals(message.snowflake, Snowflake("765903778884747295"))
        assertEquals(message.content, "lol")
        assert(message.embeds.isEmpty())
        assert(message.mentionRoles.isEmpty())
        assert(message.mentions.isEmpty())
        assert(message.attachments.isEmpty())
        assert(!message.pinned)
        assert(!message.tts)
        assert(!message.mentionEveryone)
    }
    
    @Test fun messageDelete() {
        val text = """
            {
                "id": "766110601217966103",
                "channel_id": "629123234473836545",
                "guild_id": "345259986303057930"
            }
        """.trimIndent()
        
        val event = MessageDeleteEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                       Mockito.mock(DiscordProxyKt::class.java))
        
        assertEquals(event.messageSnowflake, Snowflake("766110601217966103"))
        assertEquals(event.guild!!.snowflake, Snowflake("345259986303057930"))
        assertEquals(event.channel.snowflake, Snowflake("629123234473836545"))
    }
    
    @Test fun messageDeleteBulk() {
        val text = """
            {
                "ids": [
                    "766354693353242675", "766354690761687091", "766354689884946453", "766354688722599976",
                    "766354687711903774", "766306982121963590", "766306617045549126", "766306616092917770",
                    "766306615234134016", "766306614247555082"
                ],
                "channel_id": "484530691107717131",
                "guild_id": "345259986303057930"
            }
        """.trimIndent()
        
        val event = MessageBulkDeleteEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                                 Mockito.mock(DiscordProxyKt::class.java))
        
        assertEquals(event.messages.map { it.snowflake.id }, listOf(
            "766354693353242675", "766354690761687091", "766354689884946453", "766354688722599976",
            "766354687711903774", "766306982121963590", "766306617045549126", "766306616092917770",
            "766306615234134016", "766306614247555082"
        ))
        assertEquals(event.channel.snowflake, Snowflake("484530691107717131"))
        assertEquals(event.guild.snowflake, Snowflake("345259986303057930"))
    }
    
    @Test fun roleCreate() {
        val text = """
            {
                "role": {
                    "position": 1,
                    "permissions_new": "0",
                    "permissions": 0,
                    "name": "new role",
                    "mentionable": false,
                    "managed": false,
                    "id": "766115747633954856",
                    "hoist": false,
                    "color": 0
                },
                "guild_id": "345259986303057930",
                "guild_hashes": {
                    "version": 1,
                    "roles": {
                        "hash": "xWLYQgNUeBo"
                    },
                    "metadata": {
                        "hash": "0dViSYrsY1w"
                    },
                    "channels": {
                        "hash": "PRMJpVWdlK8"
                    }
                }
            }
        """.trimIndent()
        
        val event = RoleCreateEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                    Mockito.mock(DiscordProxyKt::class.java))
        
        assertEquals(event.guildSnowflake, Snowflake("345259986303057930"))
        
        val role = event.role
        assertEquals(role.position, 1)
        assertEquals(role.permissions, EnumSet.noneOf(Permissions::class.java))
        assertEquals(role.name, "new role")
        assertEquals(role.color, Color.black)
        assert(!role.mentionable)
        assert(!role.managed)
        assert(!role.hoisted)
    }
    
    @Test fun roleDelete() {
        val text = """
            {
                "role_id": "766340218541703228",
                "guild_id": "345259986303057930",
                "guild_hashes": {
                    "version": 1,
                    "roles": {
                        "hash": "/atQdSloXFs"
                    },
                    "metadata": {
                        "hash": "48+9PyPK/G8"
                    },
                    "channels": {
                        "hash": "PRMJpVWdlK8"
                    }
                }
            }
        """.trimIndent()
        
        val event = RoleDeleteEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                    Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.guildSnowflake, Snowflake("345259986303057930"))
        assertEquals(event.role.snowflake, Snowflake("766340218541703228"))
    }
    
    @Test fun roleUpdate() {
        val text = """
            {
                "role_id": "766115747633954856",
                "guild_id": "345259986303057930",
                "guild_hashes": {
                    "version": 1,
                    "roles": {
                        "hash": "pTnnEE1L2UU"
                    },
                    "metadata": {
                        "hash": "0dViSYrsY1w"
                    },
                    "channels": {
                        "hash": "PRMJpVWdlK8"
                    }
                }
            }
        """.trimIndent()
        
        val event = RoleUpdateEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                         Mockito.mock(DiscordProxyKt::class.java))
        
        assertEquals(event.guildSnowflake, Snowflake("345259986303057930"))
        assertEquals(event.role.snowflake, Snowflake("766115747633954856"))
    }
    
    @Test fun messageReactionAdd() {
        val text = """
            {
                "user_id": "197448151064379393",
                "message_id": "766306614247555082",
                "member": {
                    "user": {
                        "username": "deprilula28",
                        "public_flags": 131712,
                        "id": "197448151064379393",
                        "discriminator": "3609",
                        "avatar": "89a5ba9b9c465a49ee826b04d20b85f5"
                    },
                    "roles": ["460582127428435968", "484498809682919436", "484502015007785011", "484502130573443072", "484502332151824405", "484502801863409675", "484503145763045376", "484503774396809217", "484504124059156480", "484504476800122903", "484505030943047680", "484505442333098018", "484505453389152256", "484505463828905994", "484505464432754706", "484505464965562369", "484511827170951169", "484517302037905428", "484522260590493696", "484522326906503172", "484522471282704384", "484523177788047364", "484844603267219467", "507747686020022274", "507955848610316318", "507956432449306644", "514947756842811392"],
                    "premium_since": null,
                    "nick": null,
                    "mute": false,
                    "joined_at": "2017-08-10T17:39:55.480000+00:00",
                    "is_pending": false,
                    "hoisted_role": "460582127428435968",
                    "deaf": false
                },
                "emoji": {
                    "name": "trump",
                    "id": "733642150423691274"
                },
                "channel_id": "484530691107717131",
                "guild_id": "345259986303057930"
            }
        """.trimIndent()
        
        val event = MessageReactionAddEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                                 Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.messageSnowflake, Snowflake("766306614247555082"))
        assertEquals(event.user.snowflake, Snowflake("197448151064379393"))
        assertEquals(event.channel.snowflake, Snowflake("484530691107717131"))
        assertEquals(event.guild!!.snowflake, Snowflake("345259986303057930"))
        
        val emoji = event.emoji
        assertEquals(emoji.snowflake, Snowflake("733642150423691274"))
        assertEquals(emoji.name, "trump")
        
        val member = event.member!!
        assertEquals(member.roles.map { it.snowflake.id }, listOf(
            "460582127428435968", "484498809682919436", "484502015007785011",
            "484502130573443072", "484502332151824405", "484502801863409675",
            "484503145763045376", "484503774396809217", "484504124059156480",
            "484504476800122903", "484505030943047680", "484505442333098018",
            "484505453389152256", "484505463828905994", "484505464432754706",
            "484505464965562369", "484511827170951169", "484517302037905428",
            "484522260590493696", "484522326906503172", "484522471282704384",
            "484523177788047364", "484844603267219467", "507747686020022274",
            "507955848610316318", "507956432449306644", "514947756842811392"
        ))
        assert(!member.mute)
        assert(!member.deaf)
    
        val author = member.user
        assertEquals(author.username, "deprilula28")
        assertEquals(author.discriminator, "3609")
        assertEquals(author.avatarHash, "89a5ba9b9c465a49ee826b04d20b85f5")
        assertEquals(author.snowflake, Snowflake("197448151064379393"))
        assertEquals(author.publicFlags, EnumSet.of(
            User.Flags.BRILLIANCE,
            User.Flags.VERIFIED_EARLY_BOT_DEVELOPER,
            User.Flags.EARLY_SUPPORTER,
        ))
    }
    
    @Test fun messageReactionRemove() {
        val text = """
            {
                "user_id": "197448151064379393",
                "message_id": "766306617045549126",
                "emoji": {
                    "name": "???",
                    "id": null
                },
                "channel_id": "484530691107717131",
                "guild_id": "345259986303057930"
            }
        """.trimIndent()
        
        val event = MessageReactionRemoveEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                               Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.messageSnowflake, Snowflake("766306617045549126"))
        assertEquals(event.user.snowflake, Snowflake("197448151064379393"))
        assertEquals(event.guild!!.snowflake, Snowflake("345259986303057930"))
        assertEquals(event.channel.snowflake, Snowflake("484530691107717131"))
        assertEquals(event.emoji, null)
    }
    
    @Test fun mesageReactionRemoveAll() {
        val text = """
           {
                "message_id": "766306611215335425",
                "channel_id": "484530691107717131",
                "guild_id": "345259986303057930"
            }
        """.trimIndent()
        
        val event = MessageReactionRemoveAllEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                                       Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.messageSnowflake, Snowflake("766306611215335425"))
        assertEquals(event.channel.snowflake, Snowflake("484530691107717131"))
        assertEquals(event.guild!!.snowflake, Snowflake("345259986303057930"))
    }
    
    @Test fun guildCreate() {
        val text = """
            {
                "description": null,
                "system_channel_flags": 0,
                "guild_hashes": {
                    "version": 1,
                    "roles": {
                        "omitted": false,
                        "hash": "/atQdSloXFs"
                    },
                    "metadata": {
                        "omitted": false,
                        "hash": "0dViSYrsY1w"
                    },
                    "channels": {
                        "omitted": false,
                        "hash": "PRMJpVWdlK8"
                    }
                },
                "default_message_notifications": 1,
                "member_count": 5973,
                "preferred_locale": "en-US",
                "joined_at": "2019-07-12T18:38:40.508000+00:00",
                "system_channel_id": "484892069379309568",
                "application_id": null,
                "explicit_content_filter": 2,
                "voice_states": [],
                "owner_id": "699884483830349875",
                "members": [{
                    "user": {
                        "username": "GamesROB Dev",
                        "id": "387758848125042688",
                        "discriminator": "0761",
                        "bot": true,
                        "avatar": "90799be6a053cc5a0ab14f65e5741cda"
                    },
                    "roles": ["484506679707172889", "507966415379365898", "514958695356956687", "599308665660637215"],
                    "premium_since": null,
                    "nick": null,
                    "mute": false,
                    "joined_at": "2019-07-12T18:38:40.508000+00:00",
                    "is_pending": false,
                    "hoisted_role": "514958695356956687",
                    "deaf": false
                }],
                "mfa_level": 1,
                "public_updates_channel_id": "708422106874118225",
                "emojis": [{
                    "roles": [],
                    "require_colons": true,
                    "name": "AaAAAAAAAAAAAAaAAAAAAAAAAAaAAAAa",
                    "managed": false,
                    "id": "534500123942584321",
                    "available": true,
                    "animated": false
                }],
                "banner": null,
                "verification_level": 2,
                "name": "The GamesROB Server",
                "unavailable": false,
                "afk_channel_id": null,
                "channels": [{
                    "type": 0,
                    "topic": "Suggest features for the bot. Refrain from using commands here (except for presenting your suggestion)",
                    "rate_limit_per_user": 0,
                    "position": 18,
                    "permission_overwrites": [{
                        "type": "role",
                        "id": "345259986303057930",
                        "deny_new": "3342336",
                        "deny": 3342336,
                        "allow_new": "1024",
                        "allow": 1024
                    }, {
                        "type": "role",
                        "id": "484498809682919436",
                        "deny_new": "131072",
                        "deny": 131072,
                        "allow_new": "36815872",
                        "allow": 36815872
                    }, {
                        "type": "role",
                        "id": "484506679707172889",
                        "deny_new": "0",
                        "deny": 0,
                        "allow_new": "379968",
                        "allow": 379968
                    }, {
                        "type": "role",
                        "id": "553012313440124929",
                        "deny_new": "2112",
                        "deny": 2112,
                        "allow_new": "0",
                        "allow": 0
                    }],
                    "parent_id": "484529841526407184",
                    "nsfw": false,
                    "name": "suggestions",
                    "last_pin_timestamp": "2020-04-11T09:58:27.245000+00:00",
                    "last_message_id": "763218914536456213",
                    "id": "484835060009271317"
                }],
                "roles": [{
                    "position": 62,
                    "permissions_new": "512",
                    "permissions": 512,
                    "name": "Manager",
                    "mentionable": false,
                    "managed": false,
                    "id": "484517442887090177",
                    "hoist": false,
                    "color": 52224
                }],
                "region": "us-central",
                "max_members": 250000,
                "splash": null,
                "premium_subscription_count": 0,
                "vanity_url_code": null,
                "features": ["COMMERCE", "NEWS", "COMMUNITY", "WELCOME_SCREEN_ENABLED"],
                "lazy": true,
                "rules_channel_id": "516736142800846849",
                "large": true,
                "afk_timeout": 3600,
                "icon": "eacff49a38c5148b66ab2b2ae0f21d5d",
                "presences": [],
                "max_video_channel_users": 25,
                "id": "345259986303057930",
                "premium_tier": 0,
                "discovery_splash": null
            }
        """.trimIndent()
        
        val event = GuildJoinEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                   Mockito.mock(DiscordProxyKt::class.java))
        
        val guild = event.guild
        assertEquals(guild.name, "The GamesROB Server")
        assertEquals(guild.snowflake, Snowflake("345259986303057930"))
        assertEquals(guild.features, EnumSet.of(
            Features.COMMERCE,
            Features.NEWS,
            Features.WELCOME_SCREEN_ENABLED,
            Features.COMMUNITY,
        ))
        assertEquals(guild.owner.user.snowflake, Snowflake("699884483830349875"))
        assertEquals(guild.systemChannel!!.snowflake, Snowflake("484892069379309568"))
        assertEquals(guild.publicUpdatesChannel!!.snowflake, Snowflake("708422106874118225"))
        assertEquals(guild.rulesChannelSnowflake!!, Snowflake("516736142800846849"))
        assertEquals(guild.maxMembers, 250000)
        assertEquals(guild.memberCount, 5973)
        assertEquals(guild.maxVideoChannelUsers, 25)
        assertEquals(guild.afkTimeout, Timeout.SECONDS_3600)
        assertEquals(guild.explicitContentLevel, ExplicitContentFilterLevel.ALL)
        assertEquals(guild.requiredMFALevel, MFALevel.ELEVATED)
        assertEquals(guild.region, Region.US_CENTRAL)
        assertEquals(guild.locale, "en-US")
        assertEquals(guild.boosters, 0)
        assertEquals(guild.description, null)
        assertEquals(guild.iconSplash, null)
        assertEquals(guild.afkChannel, null)
        assertEquals(guild.vanityCode, null)
        assertEquals(guild.banner, null)
        assertEquals(guild.discoverableSplash, null)
        assert(guild.unavailable != true)
        assert(guild.large == true)
        
        val members = guild.cachedMembers!!
        assertEquals(members.size, 1)
        
        val member = members[0]
        assertEquals(member.premiumSince, null)
        assertEquals(member.nick, null)
        assertEquals(member.roles.map { it.snowflake.id }, listOf(
            "484506679707172889", "507966415379365898", "514958695356956687", "599308665660637215"
        ))
        assert(!member.deaf)
        assert(!member.mute)
        
        val user = member.user
        assertEquals(user.username, "GamesROB Dev")
        assertEquals(user.discriminator, "0761")
        assertEquals(user.avatarHash, "90799be6a053cc5a0ab14f65e5741cda")
        assertEquals(user.snowflake, Snowflake("387758848125042688"))
        assert(user.isBot)
        
        val channels = guild.cachedChannels!!
        assertEquals(channels.size, 1)
        
        val channel = channels[0] as TextChannel
        assertEquals(channel.name, "suggestions")
        assertEquals(channel.topic, "Suggest features for the bot. Refrain from using commands here (except for presenting your suggestion)")
        assertEquals(channel.rateLimitPerUser, 0)
        assertEquals(channel.position, 18)
        assertEquals(channel.snowflake, Snowflake("484835060009271317"))
        assertEquals(channel.category!!.snowflake, Snowflake("484529841526407184"))
        assert(!channel.nsfw)
        
        val overwrites = channel.permissions
        assertEquals(overwrites.size, 4)
        
        val overwrite = overwrites[0]
        assert(overwrite is RoleOverride)
        assertEquals(overwrite.snowflake, Snowflake("345259986303057930"))
        assertEquals(overwrite.denyRaw, 3342336)
        assertEquals(overwrite.allowRaw, 1024)
        
        val roles = guild.cachedRoles
        assertEquals(roles.size, 1)
        
        val role = roles[0]
        assertEquals(role.name, "Manager")
        assertEquals(role.snowflake, Snowflake("484517442887090177"))
        assertEquals(role.position, 62)
        assertEquals(role.permissionsRaw, 512)
        assertEquals(role.color, Color(0x00cc00))
        assert(!role.managed)
        assert(!role.mentionable)
    }
    
    @Test fun guildDelete() {
        val text = """
            {
                "id": "505164341725757480"
            }
        """.trimIndent()
        
        val event = GuildLeaveEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                         Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.guildSnowflake, Snowflake("505164341725757480"))
    }
    
    @Test fun guildBanAdd() {
        val text = """
            {
                "user": {
                    "username": "Pixal Aqua",
                    "public_flags": 128,
                    "id": "322010018235023362",
                    "discriminator": "2512",
                    "avatar": "e0f7a639999f0b48a8297c1e4a7630c7"
                },
                "guild_id": "505161921784315938"
            }
        """.trimIndent()
        
        val event = GuildBanEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                  Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.guildSnowflake, Snowflake("505161921784315938"))
        
        val user = event.user
        assertEquals(user.username, "Pixal Aqua")
        assertEquals(user.snowflake, Snowflake("322010018235023362"))
        assertEquals(user.avatarHash, "e0f7a639999f0b48a8297c1e4a7630c7")
        assertEquals(user.discriminator, "2512")
        assertEquals(user.publicFlags, EnumSet.of(User.Flags.BRILLIANCE))
    }
    
    @Test fun guildBanRemove() {
        val text = """
            {
                "user": {
                    "username": "Pixal Aqua",
                    "public_flags": 128,
                    "id": "322010018235023362",
                    "discriminator": "2512",
                    "avatar": "e0f7a639999f0b48a8297c1e4a7630c7"
                },
                "guild_id": "505161921784315938"
            }
        """.trimIndent()
        
        val event = GuildUnbanEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                    Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.guildSnowflake, Snowflake("505161921784315938"))
        
        val user = event.user
        assertEquals(user.username, "Pixal Aqua")
        assertEquals(user.snowflake, Snowflake("322010018235023362"))
        assertEquals(user.avatarHash, "e0f7a639999f0b48a8297c1e4a7630c7")
        assertEquals(user.discriminator, "2512")
        assertEquals(user.publicFlags, EnumSet.of(User.Flags.BRILLIANCE))
    }
    
    @Test fun guildMemberUpdate() {
        val text = """
            {
                "user": {
                    "username": "GamesROB Dev",
                    "id": "387758848125042688",
                    "discriminator": "0761",
                    "bot": true,
                    "avatar": "90799be6a053cc5a0ab14f65e5741cda"
                },
                "roles": ["766345421807157270"],
                "premium_since": null,
                "nick": null,
                "mute": false,
                "joined_at": "2020-10-15T17:03:06.659218+00:00",
                "is_pending": false,
                "hoisted_role": null,
                "guild_id": "505164341725757480",
                "deaf": false
            }
        """.trimIndent()
        
        val event = GuildMemberUpdateEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                                Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.guildSnowflake, Snowflake("505164341725757480"))
        
        val member = event.member
        assertEquals(member.nick, null)
        assertEquals(member.premiumSince, null)
        assert(!member.deaf)
        assert(!member.mute)
        
        val user = member.user
        assertEquals(user.username, "GamesROB Dev")
        assertEquals(user.discriminator, "0761")
        assertEquals(user.avatarHash, "90799be6a053cc5a0ab14f65e5741cda")
        assertEquals(user.snowflake, Snowflake("387758848125042688"))
        assert(user.isBot)
        
        val roles = member.roles
        assertEquals(roles.size, 1)
        
        val role = roles[0]
        assertEquals(role.snowflake, Snowflake("766345421807157270"))
    }
    
    @Test fun guildEmojisUpdate() {
        val text = """
            {
                "guild_id": "706912653829996626",
                "guild_hashes": {
                    "version": 1,
                    "roles": {
                        "hash": "/tm0U69Vd9c"
                    },
                    "metadata": {
                        "hash": "R6N7rNK4pOo"
                    },
                    "channels": {
                        "hash": "1lqKLA3GN4E"
                    }
                },
                "emojis": [{
                    "roles": [],
                    "require_colons": true,
                    "name": "1d",
                    "managed": false,
                    "id": "706918935299358861",
                    "available": true,
                    "animated": false
                }]
            }
        """.trimIndent()
        
        val event = GuildUpdateEmojisEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                                Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.guildSnowflake, Snowflake("706912653829996626"))
        
        val emojis = event.emojis
        assertEquals(emojis.size, 1)
        
        val emoji = emojis[0]
        assertEquals(emoji.name, "1d")
        assertEquals(emoji.snowflake, Snowflake("706918935299358861"))
        assert(emoji.roles.isEmpty())
        assert(!emoji.managed)
        assert(!emoji.animated)
        assert(emoji.available)
    }
    
    @Test fun inviteCreate() {
        val text = """
            {
                "uses": 0,
                "temporary": false,
                "max_uses": 0,
                "max_age": 3600,
                "inviter": {
                    "username": "deprilula28",
                    "public_flags": 131712,
                    "id": "197448151064379393",
                    "discriminator": "3609",
                    "avatar": "89a5ba9b9c465a49ee826b04d20b85f5"
                },
                "guild_id": "505161921784315938",
                "created_at": "2020-10-15T17:09:50.818097+00:00",
                "code": "6JY6CR",
                "channel_id": "505175551850315796"
            }
        """.trimIndent()
        
        val event = GuildInviteCreateEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                                Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.guildSnowflake, Snowflake("505161921784315938"))
        assertEquals(event.channel.snowflake, Snowflake("505175551850315796"))
        assertEquals(event.code, "6JY6CR")
        
        val invite = event.invite
        assertEquals(invite.uses, 0)
        assertEquals(invite.maxUses, 0)
        assertEquals(invite.maxAge, 3600)
        assert(!invite.temporary)
        
        val inviter = invite.inviter!!
        assertEquals(inviter.username, "deprilula28")
        assertEquals(inviter.discriminator, "3609")
        assertEquals(inviter.avatarHash, "89a5ba9b9c465a49ee826b04d20b85f5")
        assertEquals(inviter.snowflake, Snowflake("197448151064379393"))
        assertEquals(inviter.publicFlags, EnumSet.of(
            User.Flags.BRILLIANCE,
            User.Flags.VERIFIED_EARLY_BOT_DEVELOPER,
            User.Flags.EARLY_SUPPORTER,
        ))
    }
    
    @Test fun inviteDelete() {
        val text = """
           {
                "guild_id": "505161921784315938",
                "code": "3xcUk4",
                "channel_id": "505175551850315796"
           }
        """.trimIndent()
        
        val event = GuildInviteDeleteEvent(Json.decodeFromString(JsonObject.serializer(), text),
                                           Mockito.mock(DiscordProxyKt::class.java))
        assertEquals(event.guildSnowflake, Snowflake("505161921784315938"))
        assertEquals(event.code, "3xcUk4")
        assertEquals(event.channel.snowflake, Snowflake("505175551850315796"))
    }
}
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.discord.TextChannel
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.entities.discord.message.UnicodeEmoji
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
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
        assertEquals(message.channelSnowflake, Snowflake("290926798999357250"))
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
}

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.entities.discord.message.UnicodeEmoji
import org.junit.jupiter.api.TestInstance
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
        
        val message = Message(Json.decodeFromString(JsonObject.serializer(), text), MockDiscordProxyKt())
        
        assertEquals(message.snowflake, Snowflake("334385199974967042"))
        assertEquals(message.channelSnowflake, Snowflake("290926798999357250"))
        assertEquals(message.guildSnowflake, null)
        assertEquals(message.member, null)
        assertEquals(message.content, "Supa Hot")
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
}

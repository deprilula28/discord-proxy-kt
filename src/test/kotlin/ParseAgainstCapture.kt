import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.User
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.events.message.MessageReceivedEvent
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD) class ParseAgainstCapture {
    @Test
    fun messageCreate() {
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
        
        val event = MessageReceivedEvent(Json.decodeFromString(JsonObject.serializer(), text), Mockito.mock(DiscordProxyKt::class.java))
        
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
            "460582127428435968", "484502130573443072", "484504124059156480", "484503774396809217", "484503145763045376",
            "484505030943047680", "484505453389152256", "484505464965562369", "484511827170951169", "484522260590493696",
            "484505463828905994", "484502801863409675", "484498809682919436", "484522326906503172", "484522471282704384",
            "484844603267219467", "507747686020022274", "484505464432754706", "484517302037905428", "484502332151824405",
            "507956432449306644", "484504476800122903", "484523177788047364", "507955848610316318", "514947756842811392",
            "484505442333098018", "484502015007785011",
        ))
        assertEquals(member.premiumSince, null)
        assert(!member.mute)
        assert(!member.deaf)
    }
}
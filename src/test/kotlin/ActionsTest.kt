import kotlinx.coroutines.runBlocking
import me.deprilula28.discordproxykt.DpkBuilder
import me.deprilula28.discordproxykt.builder.MessageBuilder
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.Permissions
import me.deprilula28.discordproxykt.rest.toBitSet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.awt.Color
import java.net.URI
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS) class ActionsTest {
    @Test fun actions() {
        if (System.getenv("token") == null) return
    
        val bot = DpkBuilder(
            "stable-gateway",
            "stable",
            URI.create("amqp://localhost"),
            System.getenv("token"),
        ).build()
        
        runBlocking {
            val guild = bot.fetchGuild(Snowflake("505161921784315938")).upgrade().await()
            val selfMember = guild.fetchSelfMember.await()
            println("Self member: $selfMember")
            println("Self user permissions: ${guild.fetchUserPermissions.await()}")
            guild.fetchBans.await().forEach { println("Ban: $it") }
            guild.fetchRoles.await().forEach { println("Role: $it") }
            guild.fetchEmojis.await().forEach { println("Emoji: $it") }
            // TODO Test when audit logs are done
            //            guild.fetchAuditLogs.await()
            guild.fetchRegions.await().forEach { println("Region: $it") }
            guild.fetchChannels.await().forEach { println("Channel: $it") }
            guild.fetchWebhooks.await().forEach { println("Webhook: $it") }
            guild.fetchInvites.await().forEach { println("Invite: $it") }
        
            guild.fetchBan(bot.fetchUser(Snowflake("503720029456695306"))).await()
            guild.fetchTextChannel(Snowflake("505175551850315796")).upgrade().await()
            guild.fetchVoiceChannel(Snowflake("768067515422736424")).upgrade().await()
            guild.fetchCategory(Snowflake("505720831834456074")).upgrade().await()
            guild.fetchChannel(Snowflake("505525890709717022")).upgrade().await()
        
            // Absolutely no idea why this doesn't work:
            // Exception in thread "DefaultDispatcher-worker-3 @coroutine#1" me.deprilula28.discordproxykt.RestException: Failed request at https://discord.com/api/v8/guilds/505161921784315938/prune?days=30 (400):
            //{"message": "400: Bad Request", "code": 0}
            // println("Prunable member count: ${guild.retrievePrunableMemberCount(30).await()}")
        
            val tcr = guild.roleBuilder()
            tcr.name = "temptest"
            tcr.color = Color.red
            tcr.hoisted = true
            tcr.mentionable = true
            tcr.permissionsRaw = EnumSet.of(Permissions.MANAGE_MESSAGES).toBitSet()
            val role = tcr.create().await()
        
            role.name = "temptestv2"
            role.edit().await()
        
            selfMember.add(role).await()
            selfMember.remove(role).await()
            assert(selfMember.hasAuthorityOver(role).await())
            assert(!selfMember.hasAuthorityOver(selfMember).await())
            assertEquals(selfMember.fetchColor.await(), Color(0xe91e63))
            assert(selfMember.fetchPermissions.await().contains(Permissions.ADMINISTRATOR))
            role.delete().await()
        
            val tcb = guild.textChannelBuilder()
            tcb.name = "coolo1"
            tcb.topic = "very cool"
            tcb.rateLimitPerUser = 10
        
            val channel = tcb.create().await()
            channel.typing().await()
        
            val invite = channel.inviteBuilder().create().await()
            assert(channel.fetchInvites.await().find { it.code == invite.code } != null)
            invite.delete().await()
        
            val message = channel.send("test").await()
            message.edit("testv2").await()
            message.pin().await()
            assertEquals(channel.fetchPins.await().size, 1)
            assertEquals(channel.fetchMessages.await().size, 2) // The other message is the pin
            assertEquals(channel.fetchWebhooks.await().size, 0)
            
            val attachmentMessage = channel.send(MessageBuilder().setFile("lol.txt", "eks d")).await()
            assertEquals(attachmentMessage.attachments.size, 1)
        
            message.addReaction("🤡").await()
            message.addReaction("😞").await()
            assertEquals(message.fetchReactions("\uD83D\uDE1E").await().size, 1)
            message.clearReactions("😞").await()
            message.clearReactions().await()
        
            message.unpin().await()
            message.delete().await()
        
            channel.nsfw = true
            channel.edit().await()
        
            val other = channel.createCopy()
            other.create().await()
            other.delete().await()
        
            val category = guild.categoryBuilder()
            category.name = "neat category"
            val cat = category.create().await()
        
            val voice = guild.voiceChannelBuilder()
            voice.name = "voicetest"
            voice.userLimit = 2
            voice.category = cat
            voice.create().await()
        
            category.delete().await()
            voice.delete().await()
            channel.delete().await()
        
            guild.name = "Gameshrub Emote 1"
            guild.edit().await()
        
            val coolMember = guild.fetchMember(Snowflake("717983911824588862")).upgrade().await()
            coolMember.nick = "cool username"
            coolMember.edit().await()
        }
    }
}
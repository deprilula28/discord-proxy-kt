import kotlinx.coroutines.launch
import me.deprilula28.discordproxykt.DpkBuilder
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.Permissions
import me.deprilula28.discordproxykt.events.Events
import me.deprilula28.discordproxykt.rest.toBitSet
import org.junit.jupiter.api.TestInstance
import java.awt.Color
import java.net.URI
import java.util.*
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS) class ServiceTest {
    @Test fun test() {
        if (System.getenv("token") == null) return
        
        val bot = DpkBuilder(
            "stable-gateway",
            "stable",
            URI.create("amqp://localhost"),
            System.getenv("token"),
        ).build()
        bot.on(Events.GUILD_CREATE) { }
        bot.on(Events.GUILD_DELETE) { }
        bot.on(Events.GUILD_BAN_ADD) { }
        bot.on(Events.GUILD_BAN_REMOVE) { }
        bot.on(Events.GUILD_EMOJIS_UPDATE) { }
        bot.on(Events.GUILD_MEMBER_ADD) { }
        bot.on(Events.GUILD_MEMBER_REMOVE) { }
        bot.on(Events.GUILD_MEMBER_UPDATE) { }
        bot.on(Events.GUILD_ROLE_CREATE) { }
        bot.on(Events.GUILD_ROLE_DELETE) { }
        bot.on(Events.GUILD_ROLE_UPDATE) { }
        bot.on(Events.INVITE_CREATE) { }
        bot.on(Events.INVITE_DELETE) { }
        bot.on(Events.MESSAGE_CREATE) { msgReceived ->
            if (msgReceived.message.content.startsWith("!ratelimit")) {
                while (true) {
                    msgReceived.channel.send("this is a thing im gonna do many times").await()
                }
            }
        }
        bot.on(Events.MESSAGE_UPDATE) { }
        bot.on(Events.MESSAGE_DELETE) { }
        bot.on(Events.MESSAGE_DELETE_BULK) { }
        bot.on(Events.MESSAGE_REACTION_ADD) { }
        bot.on(Events.MESSAGE_REACTION_REMOVE) { }
        bot.on(Events.MESSAGE_REACTION_REMOVE_ALL) { }
        bot.on(Events.MESSAGE_REACTION_REMOVE_EMOJI) { }
        
        bot.scope.launch {
            val guild = bot.fetchGuild(Snowflake("505161921784315938")).upgrade().await()
            val selfMember = guild.fetchSelfMember.await()
            println("Self member: $selfMember")
            println("Self user permissions: ${guild.fetchUserPermissions.await()}")
            guild.fetchBans.await().forEach { println("Ban: $it") }
            guild.fetchRoles.await().forEach { println("Role: $it") }
            guild.fetchEmojis.await().forEach { println("Emoji: $it") }
            
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
            
            // Once again no idea why these don't work. I get a google error from them? And a 401 which is not listed anywhere in docs.
            // selfMember.add(role).await()
            // selfMember.remove(role).await()
            
            role.delete().await()
            
            // For whatever reason I don't have permission?
            // selfMember.nick = "lol"
            // selfMember.edit().await()
            
            val channel = guild.fetchTextChannel(Snowflake("505175551850315796")).upgrade().await()
            val message = channel.send("test").await()
            println("Created message: $message")
            message.edit("testv2").await()
            message.pin().await()
            message.unpin().await()
            message.delete().await()
        }
        Thread.sleep(10000000L)
    }
}
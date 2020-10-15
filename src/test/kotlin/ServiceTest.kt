import me.deprilula28.discordproxykt.DpkBuilder
import me.deprilula28.discordproxykt.events.Events
import org.junit.jupiter.api.TestInstance
import java.net.URI
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
        bot.on(Events.MESSAGE_CREATE) { }
        bot.on(Events.MESSAGE_UPDATE) { }
        bot.on(Events.MESSAGE_DELETE) { }
        bot.on(Events.MESSAGE_DELETE_BULK) { }
        bot.on(Events.MESSAGE_REACTION_ADD) { }
        bot.on(Events.MESSAGE_REACTION_REMOVE) { }
        bot.on(Events.MESSAGE_REACTION_REMOVE_ALL) { }
        bot.on(Events.MESSAGE_REACTION_REMOVE_EMOJI) { }
        Thread.sleep(10000000L)
    }
}
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.events.Events
import org.junit.jupiter.api.TestInstance
import java.net.URI
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS) class ServiceTest {
    @Test fun test() {
        if (System.getenv("token") == null) return
        
        val bot = DiscordProxyKt.Companion.Builder(
            "stable-gateway",
            "stable",
            URI.create("amqp://localhost"),
            System.getenv("token")
        ).build()
        bot.on(Events.MESSAGE_CREATE) {
            println(it)
        }
        bot.on(Events.MESSAGE_UPDATE) {
            println(it)
        }
        Thread.sleep(10000000L)
    }
}
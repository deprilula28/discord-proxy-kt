import me.deprilula28.discordproxykt.DiscordProxyKt
import org.junit.jupiter.api.TestInstance
import java.net.URI
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS) class ServiceTest {
    @Test
    fun test() {
        val bot = DiscordProxyKt.Companion.Builder("stable-gateway", "stable", URI.create("amqp://localhost"), System.getenv("token"))
        bot.build().subscribe("MESSAGE_CREATE", "MESSAGE_UPDATE")
        Thread.sleep(10000000L)
    }
}

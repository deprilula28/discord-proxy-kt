import me.deprilula28.discordproxykt.JdaProxySpectacles
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import java.net.URI

@TestInstance(TestInstance.Lifecycle.PER_CLASS) class ServiceTest {
    @Test
    fun test() {
        val proxy = JdaProxySpectacles("stable-gateway", "stable", URI.create("amqp://localhost"))
        proxy.subscribe("MESSAGE_CREATE")
        Thread.sleep(10000000L)
    }
}

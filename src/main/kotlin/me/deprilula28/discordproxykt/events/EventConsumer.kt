package me.deprilula28.discordproxykt.events

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import me.deprilula28.discordproxykt.DiscordProxyKtImpl

class EventConsumer(private val spec: DiscordProxyKtImpl, private val event: String, channel: Channel): DefaultConsumer(
    channel) {
    override fun handleDelivery(
        consumerTag: String?,
        envelope: Envelope,
        properties: AMQP.BasicProperties?,
        body: ByteArray,
    ) {
        spec.pool.submit {
            println("Handle delivery of $event body: ${body.toString(Charsets.UTF_8)}")
            channel.basicAck(envelope.deliveryTag, false)
        }
    }
}
package com.qqcr.springboot.rabbitmq.consumer_ack.consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class RabbitMqListener {
    @RabbitListener(queues = "dlx_nack_queue")
    public void nackNotRequeueListener(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException, InterruptedException {
        System.out.println("consumerAckQueue_nack_not_requeue message: " + new String(message.getBody()));
        // 收到消息，但是将消息进行nack处理，调用nack方法，切requeue参数设置为false，则消息不会重新入队，会被发送到死信交换机
        channel.basicNack(tag, false, false);
    }
}

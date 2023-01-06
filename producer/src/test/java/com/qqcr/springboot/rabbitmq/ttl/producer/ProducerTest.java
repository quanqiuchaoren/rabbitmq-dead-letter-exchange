package com.qqcr.springboot.rabbitmq.ttl.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ProducerTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void send_ttl_message_to_ttl_queue() {
        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {

            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //1.设置message的信息
                message.getMessageProperties().setExpiration("20000");//消息的过期时间
                //2.返回该消息
                return message;
            }
        };
        rabbitTemplate.convertAndSend("", RabbitMQConfig.TTL_QUEUE, "hello, ttl queue", messagePostProcessor);
    }


    /**
     * 有两条消息超过队列最大长度，则队列头的两条消息会成为死信。
     * 即，
     *      第5条消息发送到MAX_LENGTH_QUEUE队列中的时候，第0条队列会成为死信。
     *      第6条消息发送到MAX_LENGTH_QUEUE队列中的时候，第1条队列会成为死信。
     *      最后，MAX_LENGTH_QUEUE中剩下的消息第第2/3/4/5/6条消息。而第0/1条消息成为死信。队列头的消息最先成为死信。
     */
    @Test
    public void send_ttl_message_to_max_length_queue() throws InterruptedException {
        for (int i = 0; i < 7; i++) {
            rabbitTemplate.convertAndSend("", RabbitMQConfig.MAX_LENGTH_QUEUE, "hello, max length queue: " + i);
            TimeUnit.SECONDS.sleep(5);
        }
    }

    /**
     * 消费者使用nack方法拒收消息，且不进行requeue，则消息成为死信
     */
    @Test
    public void consumer_nack_message() throws InterruptedException {
            rabbitTemplate.convertAndSend("", RabbitMQConfig.NACK_QUEUE, "hello, consumer nack message");
    }
}

package com.qqcr.springboot.rabbitmq.ttl.producer;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    public static final String DLX_EXCHANGE = "dlx_topic_exchange";
    public static final String TTL_QUEUE = "dlx_ttl_queue";
    public static final String MAX_LENGTH_QUEUE = "dlx_max_length_queue";
    public static final String NACK_QUEUE = "dlx_nack_queue";
    public static final String DLX_QUEUE = "dlx_queue";

    /**
     * 这个队列会设置最长长度，所以会产生死信
     */
    @Bean(MAX_LENGTH_QUEUE)
    public Queue maxLengthQueue() {
        Map<String, Object> arguments = new HashMap<>();
        // 队列的死信交换机
        arguments.put("x-dead-letter-exchange", DLX_EXCHANGE);
        // 设置将死信转发到死信交换机时的key
        arguments.put("x-dead-letter-routing-key", "dlx.max.length");
        // 设置消息队列的最大长度，在达到最大长度后，如果又收到消息，则该消息成为死信
        arguments.put("x-max-length", 5);
        return QueueBuilder.durable(MAX_LENGTH_QUEUE).withArguments(arguments).build();
    }

    /**
     * 发送到这个交换机的消息，会设置过期时间，所以会产生死信
     */
    @Bean(TTL_QUEUE)
    public Queue ttlQueue() {
        Map<String, Object> arguments = new HashMap<>();
        // 队列的死信交换机
        arguments.put("x-dead-letter-exchange", DLX_EXCHANGE);
        // 设置将死信转发到死信交换机时的key
        arguments.put("x-dead-letter-routing-key", "dlx.ttl");
        return QueueBuilder.durable(TTL_QUEUE).withArguments(arguments).build();
    }

    /**
     * 监听这个队列的消费者，会拒收消息，所以会产生死信
     */
    @Bean(NACK_QUEUE)
    public Queue nackQueue() {
        Map<String, Object> arguments = new HashMap<>();
        // 设置队列的死信交换机
        arguments.put("x-dead-letter-exchange", DLX_EXCHANGE);
        // 设置将死信转发到死信交换机时的key
        arguments.put("x-dead-letter-routing-key", "dlx.nack");
        return QueueBuilder.durable(NACK_QUEUE).withArguments(arguments).build();
    }

    /**
     * 定义死信队列。
     * 死信队列和普通队列没有什么区别，只不过存储的是死信。
     */
    @Bean(DLX_QUEUE)
    public Queue dlxQueue() {
        return QueueBuilder.durable(DLX_QUEUE).build();
    }

    /**
     * 定义一个交换机，作为死信交换机。
     * 死信交换机和普通的交换机没什么不同，功能都是相似的。
     */
    @Bean(DLX_EXCHANGE)
    public Exchange bootExchange() {
        return ExchangeBuilder.topicExchange(DLX_EXCHANGE).durable(true).build();
    }

    /**
     * 队列和交互机绑定关系 Binding
     * 1. 知道哪个队列
     * 2. 知道哪个交换机
     * 3. routing key
     *
     * @param queue    知道哪个队列
     * @param exchange 知道哪个交换机
     * @return 交换机和队列的绑定
     */
    @Bean
    public Binding bindAck(@Qualifier(DLX_QUEUE) Queue queue, @Qualifier(DLX_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("dlx.#").noargs();
    }
}

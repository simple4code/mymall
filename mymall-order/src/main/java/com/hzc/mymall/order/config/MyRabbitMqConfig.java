package com.hzc.mymall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 0:40
 */
@Configuration
@Slf4j
public class MyRabbitMqConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 将Jackson2JsonMessageConverter放入容器中，RabbitTemplate初始化时就会使用该序列化器，而不是使用默认的序列化器
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制 RabbitTemplate
     * 1，消息抵达服务器进行回调
     *      1)，spring.rabbitmq.publisher-confirm=true
     *      2)，设置确认回调 ConfirmCallback
     * 2，消息抵达队列进行回调
     *      1)，spring.rabbitmq.publisher-returns = true
     *          spring.rabbitmq.template.mandatory = true
     *      2)，设置队列确认回调 ReturnCallback
     * @PostConstruct 注解，当前对象创建完成后会执行该方法
     */
    @PostConstruct
    public void initRabbitTemplate() {
        // 设置消息抵达服务器确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *  只要消息抵达服务器，即使消息没有被消费，ack也为true
             * @param correlationData  前消息的唯一关联数据（可以视作消息的唯一id）
             * @param ack  消息是否成功收到
             * @param cause  失败原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("confirm...correlationData[{}] ==> ack[{}] ==> cause[{}]",
                        correlationData,
                        ack,
                        cause);
            }
        });

        // 设置消息抵达队列确认回调（消息未抵达，就会回调该方法）
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有抵达队列，就会触发该回调方法
             * @param message    投递失败的消息的详细信息
             * @param replyCode  回复的状态码
             * @param replyText  回复的文本内容
             * @param exchange   消息发送给哪个交换机
             * @param routingKey 路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("return...Fail message[{}], replyCode[{}], replyText[{}], exchange[{}], routingKey[{}]",
                        message,
                        replyCode,
                        replyText,
                        exchange,
                        routingKey);
            }
        });
    }
}

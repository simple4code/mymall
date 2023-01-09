package com.hzc.mymall.order;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AmqpAdminTests {

	@Autowired
	private AmqpAdmin amqpAdmin;

	/**
	 * 1，如何创建 Exchange，Queue，Binding
	 *
	 * 2，如何收发消息
	 */
	@Test
	public void createExchange() {
		// 创建 Exchange
		DirectExchange directExchange = new DirectExchange("hello-java-exchange",
				true,
				false);
		amqpAdmin.declareExchange(directExchange);
		log.info("Exchange【{}】创建成功", "hello-java-exchange");
	}

	@Test
	public void createQueue() {
		// 创建 Queue
		Queue queue = new Queue("hello-java-queue",
				true,
				false,
				false);
		amqpAdmin.declareQueue(queue);
		log.info("Queue【{}】创建成功", "hello-java-queue");
	}

	@Test
	public void createBinding() {
		// 创建 Binding
		// String destination 【目的地】该绑定要绑定到哪里
		// DestinationType destinationType 【目的地类型】队列或交换机
		// String exchange 【交换机】
		// String routingKey 【路由键】
		// Map<String, Object> arguments 【自定义参数】
		// 将 exchange 指定的交换机和 destination 目的地进行绑定，使用 routingKey 作为指定的路由键
		Binding binding = new Binding("hello-java-queue",
				Binding.DestinationType.QUEUE,
				"hello-java-exchange",
				"hello.java",
				null);
		amqpAdmin.declareBinding(binding);
		log.info("Binding【{}】创建成功", "hello-java-binding");
	}
}

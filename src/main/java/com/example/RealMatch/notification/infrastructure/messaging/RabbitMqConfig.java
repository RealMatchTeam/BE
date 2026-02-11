package com.example.RealMatch.notification.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 인프라 설정
 *
 * <p>토폴로지:
 * <pre>
 * notification.exchange ──routing──▶ notification.delivery.queue
 *                                        │ (x-dead-letter-exchange)
 *                                        ▼
 * notification.dlx ──────routing──▶ notification.delivery.dlq
 * </pre>
 *
 * <p>설계 원칙:
 * <ul>
 *   <li>Manual ACK: Consumer가 처리 완료/실패를 명시적으로 결정</li>
 *   <li>DLQ: 처리 불가 메시지를 Dead Letter Queue로 격리</li>
 *   <li>JSON 직렬화: 메시지 디버깅과 모니터링 용이</li>
 *   <li>prefetch=10: Consumer가 한 번에 가져오는 메시지 수 제한</li>
 * </ul>
 */
@Configuration
public class RabbitMqConfig {

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.delivery.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.delivery";

    public static final String NOTIFICATION_DLX = "notification.dlx";
    public static final String NOTIFICATION_DLQ = "notification.delivery.dlq";
    public static final String NOTIFICATION_DLQ_ROUTING_KEY = "notification.delivery.dead";

    // ==================== Exchange ====================

    @Bean
    DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange notificationDlx() {
        return new DirectExchange(NOTIFICATION_DLX, true, false);
    }

    // ==================== Queue ====================

    /**
     * 메인 큐: DLX 설정으로 처리 불가 메시지 자동 이동.
     */
    @Bean
    Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ_ROUTING_KEY)
                .build();
    }

    /**
     * Dead Letter Queue: 최종 실패 메시지 보관. 운영팀 모니터링 대상.
     */
    @Bean
    Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    // ==================== Binding ====================

    @Bean
    Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    Binding notificationDlqBinding(Queue notificationDlq, DirectExchange notificationDlx) {
        return BindingBuilder.bind(notificationDlq)
                .to(notificationDlx)
                .with(NOTIFICATION_DLQ_ROUTING_KEY);
    }

    // ==================== Converter & Template ====================

    @Bean
    MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                  MessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter);
        template.setChannelTransacted(false);
        return template;
    }

    // ==================== Listener Container ====================

    /**
     * Manual ACK 모드 컨테이너 팩토리.
     * Consumer가 basicAck/basicNack를 직접 호출하여
     * 메시지 유실이나 무한 redelivery를 방지한다.
     */
    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jackson2JsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}

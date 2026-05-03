package com.shopzone.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Creates Kafka topics on application startup.
 * Spring Kafka's AdminClient auto-creates these if they don't exist.
 *
 * 3 partitions per topic for parallelism.
 * Replication factor 1 for local dev (single broker).
 */
@Configuration
public class KafkaTopicConfig {

    public static final String ORDER_EVENTS_TOPIC = "shopzone.order.events";
    public static final String STOCK_EVENTS_TOPIC = "shopzone.stock.events";
    public static final String PAYMENT_EVENTS_TOPIC = "shopzone.payment.events";
    public static final String NOTIFICATION_EVENTS_TOPIC = "shopzone.notification.events";

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(ORDER_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockEventsTopic() {
        return TopicBuilder.name(STOCK_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(PAYMENT_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(NOTIFICATION_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

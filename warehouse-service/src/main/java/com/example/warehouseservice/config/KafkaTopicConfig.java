package com.example.warehouseservice.config;

import lombok.Getter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Getter
public class KafkaTopicConfig {

    @Value("${kafka.orders.topic}")
    private String ordersTopicName;
    @Value("${kafka.ordersStatusUpdate.topic}")
    private String ordersStatusUpdateName;
    @Value("${kafka.factory.topic}")
    private String factorySuppliedItemsTopicName;
    @Value("${kafka.itemRequest.topic}")
    private String itemRequestTopicName;

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(ordersTopicName)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ordersStatusUpdate() {
        return TopicBuilder.name(ordersStatusUpdateName)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic factorySuppliedItemsTopicName() {
        return TopicBuilder.name(factorySuppliedItemsTopicName)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic itemRequestTopicName() {
        return TopicBuilder.name(itemRequestTopicName)
                .partitions(1)
                .replicas(1)
                .build();
    }

}
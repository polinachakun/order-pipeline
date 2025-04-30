package com.example.config;

import lombok.Getter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Getter
public class KafkaTopicConfig {

    @Value("${kafka.stockAddedTopic.factory-service-topic}")
    private String stockAddedTopicName;

    @Value("${kafka.itemRequestTopic.warehouse-service-topic}")
    private String itemRequestTopicName;

    @Bean
    public NewTopic stockAddedTopic() {
        return TopicBuilder.name(stockAddedTopicName)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic itemRequestTopic() {
        return TopicBuilder.name(itemRequestTopicName)
                .partitions(1)
                .replicas(1)
                .build();
    }

}
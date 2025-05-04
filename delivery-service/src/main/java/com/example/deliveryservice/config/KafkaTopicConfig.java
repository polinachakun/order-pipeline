package com.example.deliveryservice.config;

import lombok.Getter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Getter
public class KafkaTopicConfig {

    @Value("${kafka.ordersStatusUpdate.order-service-topic}")
    private String orderServiceStatusUpdateName;

    @Value("${kafka.ordersStatusUpdate.warehouse-service-topic}")
    private String ordersStatusUpdateName;

//    @Value("${kafka.ordersStatusUpdate.delivered-order-service-topic}")
//    private String deliveredOrdersStatusUpdateName;

    @Bean
    public NewTopic orderServiceStatusUpdateTopic() {
        return TopicBuilder.name(orderServiceStatusUpdateName)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ordersStatusUpdateTopic() {
        return TopicBuilder.name(ordersStatusUpdateName)
                .partitions(1)
                .replicas(1)
                .build();
    }

//    @Bean
//    public NewTopic deliveredOrdersStatusUpdate() {
//        return TopicBuilder.name(deliveredOrdersStatusUpdateName)
//                .partitions(1)
//                .replicas(1)
//                .build();
//    }

}
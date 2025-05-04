package com.example.deliveryservice.publisher;

import com.example.deliveryservice.config.KafkaTopicConfig;
import com.example.deliveryservice.dto.AbstractDto;
import com.example.deliveryservice.dto.OrderStatusUpdateEventDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DeliveryEventPublisher {

    private final KafkaTemplate<String, AbstractDto> objectKafkaTemplate;
    private final KafkaTopicConfig kafkaTopicConfig;

    public void publishOrderStatusUpdate(OrderStatusUpdateEventDto event) {
        publishEvent(kafkaTopicConfig.getOrderServiceStatusUpdateName(), event);
    }

    public void publishDeliveryFailedOrderStatusUpdate(OrderStatusUpdateEventDto event) {
        publishEvent(kafkaTopicConfig.getOrdersStatusUpdateName(), event);
    }

    public void publishCompletedDeliveryOrderStatusUpdate(OrderStatusUpdateEventDto event) {
        publishEvent(kafkaTopicConfig.getOrdersStatusUpdateName(), event);
    }

    private void publishEvent(String topicName, OrderStatusUpdateEventDto event) {
        log.info("Publishing status update to {}, order {} -> status {}",
                topicName, event.getOrderId(), event.getStatus());

        objectKafkaTemplate.send(topicName, event);
    }

}
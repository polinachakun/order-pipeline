package com.example.warehouseservice.publisher;

import com.example.warehouseservice.config.KafkaTopicConfig;
import com.example.warehouseservice.dto.AbstractDto;
import com.example.warehouseservice.dto.ItemDto;
import com.example.warehouseservice.dto.OrderStatusUpdateEventDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class WarehouseEventPublisher {

    private final KafkaTemplate<String, AbstractDto> objectKafkaTemplate;
    private final KafkaTopicConfig kafkaTopicConfig;

    public void publishOrderStatusUpdate(OrderStatusUpdateEventDto event) {
        log.info("Publishing order status update to {}, {} -> {}",
                kafkaTopicConfig.getOrdersStatusUpdateName(), event.getOrderId(), event.getStatus());

        objectKafkaTemplate.send(kafkaTopicConfig.getOrdersStatusUpdateName(), event);
    }

    public void publishItemRequest(ItemDto itemDto) {
        log.info("Publishing item request to {}, itemId={}, Quantity={}",
                kafkaTopicConfig.getItemRequestTopicName(), itemDto.getItemId(), itemDto.getQuantity());

        objectKafkaTemplate.send(kafkaTopicConfig.getItemRequestTopicName(), itemDto);
    }

}